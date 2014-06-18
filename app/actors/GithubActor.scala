package actors

import actors.compute.G1.G1Actor
import akka.actor._
import models.GithubRepository
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsArray, JsObject}
import play.api.libs.ws.{WS, WSResponse}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

// TODO 1 : gérer les headers rate-limits : https://developer.github.com/v3/#rate-limiting
// TODO 1.1 : Les rates limites peuvent être géré avec ça : https://developer.github.com/v3/rate_limit/
// TODO 2 (maybe) : Utiliser les conditional request pour baisser le nombre de requests nécessaire : https://developer.github.com/v3/#conditional-requests

case class RepositoryData(repo: GithubRepository, issues: List[JsObject])

object GithubActor {

  val githubApiUrl = "https://api.github.com"

  import play.api.Play

  val client_id = Play.current.configuration.getString("github.client.id").get
  val client_secret = Play.current.configuration.getString("github.client.secret").get
}

class GithubActor extends Actor with ActorLogging {

  import play.api.Play.current

  var g1Calculator: ActorRef = null
  var repository: GithubRepository = null
  val issues = new ListBuffer[JsObject]()

  override def receive: Receive = {

    case repo: GithubRepository =>
      log.debug(s"${this.getClass} | Next Repo : ${repo.owner}/${repo.name}")

      g1Calculator = Akka.system.actorOf(Props[G1Actor], s"${repo.owner}_${repo.name}_calculator")

      this.repository = repo

      getIssues(repo.owner, repo.name)
        .map {
        response =>
          handleGithubResponse(response)
      }

    case link: String =>
      log.debug(s"${this.getClass} | Next link : ${link.substring(link.indexOf("sort=created&page=") + "sort=created&page=".size, link.size)}")

      WS.url(link)
        .withQueryString(
          "client_id" -> GithubActor.client_id,
          "client_secret" -> GithubActor.client_secret
        )
        .get()
        .map {
        response =>
          handleGithubResponse(response)
      }

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
    WS.url(GithubActor.githubApiUrl + s"/repos/$owner/$repo/issues")
      .withQueryString(
        "per_page" -> "100",
        "state" -> "all",
        "sort" -> "created",
        "direction" -> "asc"
      )
      .withQueryString(
        "client_id" -> GithubActor.client_id,
        "client_secret" -> GithubActor.client_secret
      ).get()
  }

  private def handleGithubResponse(response: WSResponse) {
    response.status match {
      case 200 =>
        handleGithubOkResponse(response)
      case _ =>
        handleGithubErrorResponse(response)
        self ! PoisonPill
    }
  }

  /**
   *
   * @param response
   */
  private def handleGithubOkResponse(response: WSResponse) {
    issues ++= response.json.asInstanceOf[JsArray].value.asInstanceOf[Seq[JsObject]]

    response.header("Link") match {
      case linkHeader: Some[String] =>
        parseLinkHeader(linkHeader.get).get("next") match {
          case nextLink: Some[String] =>
            self ! nextLink.get
          case _ =>
            sendDataAndDie()
        }
      case None =>
        sendDataAndDie()
    }
  }

  private def sendDataAndDie(): Unit = {
    g1Calculator ! RepositoryData(repository, issues.toList)
    self ! PoisonPill
  }


  // TODO : Améliorer la gestion des réponses non 200
  /**
   * Documentation des erreurs de l'API Github :
   *
   * https://developer.github.com/v3/#client-errors
   *
   * @param response
   */
  private def handleGithubErrorResponse(response: WSResponse) = {
    log.error(s"${this.getClass} | Erreur Github : ${response.json \ "message"}")
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
  private def parseLinkHeader(linkHeader: String): mutable.Map[String, String] = {
    val linkMap = mutable.Map[String, String]()
    linkHeader.split(',') map {
      part =>
        val section = part.split(';')
        val url = section(0).replace("<", "").replace(">", "")
        val name = section(1).replace(" rel=\"", "").replace("\"", "")
        linkMap(name) = url
    }
    linkMap
  }

}
