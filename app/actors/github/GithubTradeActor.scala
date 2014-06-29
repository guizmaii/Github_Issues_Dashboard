package actors.github

import actors.compute.G1.G1Actor
import actors.compute.G2.G2Actor
import actors.compute.G4.G4Actor
import akka.actor._
import models.GithubRepository
import play.api.libs.concurrent.Akka
import play.api.libs.json.JsObject
import spray.client.pipelining._
import spray.http._

import scala.collection.immutable.TreeMap
import scala.concurrent.Future
import scala.util.{Failure, Success}

// TODO 1 : gérer les headers rate-limits : https://developer.github.com/v3/#rate-limiting
// TODO 1.1 : Les rates limites peuvent être géré avec ça : https://developer.github.com/v3/rate_limit/
// TODO 2 (maybe) : Utiliser les conditional request pour baisser le nombre de requests nécessaire : https://developer.github.com/v3/#conditional-requests

case class RepositoryData(repo: GithubRepository, issues: List[JsObject])

case class CalculationFinishedEvent()

object GithubTradeActor {

  val githubApiUrl = "https://api.github.com"

  import play.api.Play

  val client_id = Play.current.configuration.getString("github.client.id").get
  val client_secret = Play.current.configuration.getString("github.client.secret").get
}

class GithubTradeActor extends AbstractGithubActor {

  import play.api.Play.current

  private val calculatorsList: List[ActorRef] = List(
    context.actorOf(Props[G1Actor], "G1_Actor"),
    context.actorOf(Props[G2Actor], "G2_Actor"),
    context.actorOf(Props[G4Actor], "G4_Actor")
  )

  private var nbCalculator = calculatorsList.size

  private var repository: GithubRepository = null
  private var childrenResponses = TreeMap[Int, List[JsObject]]()

  private var nbPage = 0

  var begin = 0L
  var end = 0L

  // Needed by spray-client
  implicit val system = Akka.system
  import system.dispatcher // execution context for futures

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  override def receive: Receive = {

    case repo: GithubRepository =>
      log.debug(s"Next Repo : ${GithubTradeActor.githubApiUrl}/${repo.owner}/${repo.name}")
      begin = System.currentTimeMillis()

      this.repository = repo

      getIssues(repo.owner, repo.name) onComplete {
        case Success(response) =>
          handleSuccessResponse(response)

        case Failure(error) =>
          handleFailureResponse(error)
          context.stop(self)
      }

    case tuple: (Int, List[JsObject]) =>
      childrenResponses = childrenResponses + tuple
      if (allGettersAnswered) {
        sendDataToCalculators()
      }

    case cfe: CalculationFinishedEvent =>
      nbCalculator -= 1
      if (allCalculatorsHaveFinished) {
        end = System.currentTimeMillis()
        log.debug("Temps de total (récupération des données + calcul) : " + ((end - begin) / 1000) + " secondes")
        context.stop(self)
      }

  }

  def allCalculatorsHaveFinished: Boolean = {
    nbCalculator == 0
  }

  private def allGettersAnswered: Boolean = {
    childrenResponses.size == nbPage
  }

  /**
   * List issues for a repository
   *
   * GET /repos/:owner/:repo/issues
   *
   * @param owner
   * @param repo
   * @return
   */
  private def getIssues(owner: String, repo: String): Future[HttpResponse] = {
    pipeline(
      Get(
        s"${GithubTradeActor.githubApiUrl}/repos/$owner/$repo/issues" +
          s"?client_id=${GithubTradeActor.client_id}" +
          s"&client_secret=${GithubTradeActor.client_secret}" +
          s"&per_page=${100}" +
          s"&state=${"all"}" +
          s"&sort=${"created"}" +
          s"&direction=${"asc"}"
      )
    )
  }

  /**
   *
   * @param response
   */
  private def handleSuccessResponse(response: HttpResponse): Unit = {
    childrenResponses = childrenResponses + (1 -> convertResponseToJsObjectList(response))

    response.headers exists { _.lowercaseName == "link" }  match {
      case false => sendDataToCalculators()
      case true =>
        val linkHeader = (response.headers find { _.lowercaseName == "link" }).get
        val parsedLinkHeader = parseLinkHeader(linkHeader.value)
        parsedLinkHeader.get("last") match {
          case None => sendDataToCalculators()
          case lastLink: Some[String] =>
            val next = getPageIndexFromLink(parsedLinkHeader.get("next").get)
            nbPage = getPageIndexFromLink(lastLink.get)
            for (i <- next to nbPage) {
              context.actorOf(Props[GithubSingleGetterActor], s"getter_$i") ! constructLink(lastLink.get, i)
            }
        }
    }
  }

  private def constructLink(link: String, pageIndex: Int): String = {
    s"${link.split("&page=")(0)}&page=$pageIndex".trim
  }

  private def sendDataToCalculators(): Unit = {
    val data = RepositoryData(repository, childrenResponses.map(_._2).flatten.toList)
    calculatorsList map { _ ! data }
  }

  /**
   * Parse the Github Link HTTP header used for pagination
   *
   * http://developer.github.com/v3/#pagination
   *
   * Original code found here : https://gist.github.com/niallo/3109252
   *
   * @param linkHeader
   * @return
   */
  private def parseLinkHeader(linkHeader: String): Map[String, String] = {
    (linkHeader.split(',') map {
      part =>
        val section = part.split(';')
        val url = section(0).replace("<", "").replace(">", "")
        val name = section(1).replace(" rel=", "")
        (name, url)
    }).toMap
  }

}
