package actors

import actors.compute.G1.{CalculationFinishedEvent, G1Actor}
import akka.actor._
import models.GithubRepository
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsObject
import play.api.libs.ws.{WS, WSResponse}

import scala.collection.immutable.TreeMap
import scala.concurrent.Future

// TODO 1 : gérer les headers rate-limits : https://developer.github.com/v3/#rate-limiting
// TODO 1.1 : Les rates limites peuvent être géré avec ça : https://developer.github.com/v3/rate_limit/
// TODO 2 (maybe) : Utiliser les conditional request pour baisser le nombre de requests nécessaire : https://developer.github.com/v3/#conditional-requests

case class RepositoryData(repo: GithubRepository, issues: List[JsObject])

object GithubTradeActor {

  val githubApiUrl = "https://api.github.com"

  import play.api.Play

  val client_id = Play.current.configuration.getString("github.client.id").get
  val client_secret = Play.current.configuration.getString("github.client.secret").get
}

class GithubTradeActor extends AbstractGithubActor {

  import play.api.Play.current

  private var g1Calculator: ActorRef = null
  private var repository: GithubRepository = null

  private var responses = TreeMap[Int, List[JsObject]]()

  private var nbPage = 0

  var begin = 0L
  var end = 0L

  override def receive: Receive = {

    case repo: GithubRepository =>
      begin = System.currentTimeMillis()

      log.debug(s"Next Repo : ${GithubTradeActor.githubApiUrl}/${repo.owner}/${repo.name}")

      g1Calculator = context.actorOf(Props[G1Actor], "G1Calculator")

      this.repository = repo

      getIssues(repo.owner, repo.name)
        .map {
        response =>
          handleGithubResponse(response)
      }

    case tuple: (Int, List[JsObject]) =>
      responses = responses + tuple
      if (responses.size == nbPage) {
        g1Calculator ! RepositoryData(repository, responses.map(_._2).flatten.toList)
      }

    case cfe: CalculationFinishedEvent =>
      end = System.currentTimeMillis()
      log.debug("Temps de total (récupération des données + calcul) : " + ((end - begin) / 1000) + " secondes")
      context.stop(self)
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
  private def getIssues(owner: String, repo: String): Future[WSResponse] = {
    WS.url(GithubTradeActor.githubApiUrl + s"/repos/$owner/$repo/issues")
      .withQueryString(
        "per_page" -> "100",
        "state" -> "all",
        "sort" -> "created",
        "direction" -> "asc"
      )
      .withQueryString(
        "client_id" -> GithubTradeActor.client_id,
        "client_secret" -> GithubTradeActor.client_secret
      ).get()
  }

  /**
   *
   * @param response
   */
  override protected def handleOkResponse(response: WSResponse): Unit = {
    responses = responses + (1 -> convertResponseToJsObjectList(response))

    response.header("Link") match {
      case linkHeader: Some[String] =>
        parseLinkHeader(linkHeader.get).get("last") map {
          case lastLink: String =>
            val next = getPageIndexFromLink(parseLinkHeader(linkHeader.get).get("next").get)
            nbPage = getPageIndexFromLink(lastLink)
            for (i <- next to nbPage) {
              context.actorOf(Props[GithubSingleGetterActor], s"getter_$i") ! constructLink(lastLink, i)
            }

          case _ =>
            sendDataToCalculator()
        }

      case None =>
        sendDataToCalculator()
    }
  }

  private def constructLink(link: String, pageIndex: Int): String = {
    s"${link.split("&page=")(0)}&page=$pageIndex".trim
  }

  private def sendDataToCalculator(): Unit = {
    g1Calculator ! RepositoryData(repository, responses.map{ tuple => tuple._2 }.flatten.toList)
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
        val name = section(1).replace(" rel=\"", "").replace("\"", "")
        (name, url)
    }).toMap
  }

}
