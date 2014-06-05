package actors

import akka.actor.{PoisonPill, Props, Actor}
import scala.concurrent.Future
import play.api.libs.ws.{WS, Response}

import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.mutable
import play.api.Logger
import play.api.libs.json.{JsValue, JsArray}
import play.api.libs.concurrent.Akka
import scala.collection.mutable.ListBuffer

// TODO 1 : gérer les headers rate-limits : https://developer.github.com/v3/#rate-limiting
// TODO 1.1 : Les rates limites peuvent être géré avec ça : https://developer.github.com/v3/rate_limit/
// TODO 2 (maybe) : Utiliser les conditional request pour baisser le nombre de requests nécessaire : https://developer.github.com/v3/#conditional-requests

case class GithubRepository(owner: String, name: String)

case class RepositoryData(name: String, owner: String, issues: List[JsValue])

object GithubActor {

  val githubApiUrl = "https://api.github.com"

  import play.api.Play

  val client_id = Play.current.configuration.getString("github.client.id").get
  val client_secret = Play.current.configuration.getString("github.client.secret").get
}

class GithubActor extends Actor {

  import play.api.Play.current

  var parserActor = Akka.system.actorOf(Props[IssueParserActor])

  val repoName = new StringBuffer()
  val repoOwner = new StringBuffer()

  val issues = new ListBuffer[JsValue]()

  override def receive: Receive = {

    case repo: GithubRepository =>
      Logger.debug(s"${this.getClass} | Next Repo : ${repo.owner}/${repo.name}")

      repoName append repo.name
      repoOwner append repo.owner

      getIssues(repo.owner, repo.name)
        .map {
        response =>
          handleGithubResponse(response)
      }

    case link: String =>
      Logger.debug(s"${this.getClass} | Next call : $link")

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

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      throw error
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
  private def getIssues(owner: String, repo: String): Future[Response] = {
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

  private def handleGithubResponse(response: Response) {
    response.status match {
      case 200 => handleGithubOkResponse(response)
      case _ =>
        handleGithubErrorResponse(response)
        self ! PoisonPill
    }
  }

  /**
   *
   * @param response
   */
  private def handleGithubOkResponse(response: Response) {
    issues ++= response.json.asInstanceOf[JsArray].value

    parseLinkHeader(response.header("Link").get).get("next") match {
      case nextLink: Some[String] =>
        self ! nextLink.get
      case _ =>
        parserActor ! RepositoryData(repoName.toString, repoOwner.toString, issues.toList)
        self ! PoisonPill
    }
  }

  // TODO : Améliorer la gestion des réponses non 200
  /**
   * Documentation des erreurs de l'API Github :
   *
   * https://developer.github.com/v3/#client-errors
   *
   * @param response
   */
  private def handleGithubErrorResponse(response: Response) = {
    Logger.error(s"${this.getClass} | Erreur Github : ${response.json \ "message"}")
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
