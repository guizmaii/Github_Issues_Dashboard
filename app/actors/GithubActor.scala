package actors

import akka.actor.{Props, Actor}
import scala.concurrent.Future
import play.api.libs.ws.{WS, Response}

import play.api.libs.concurrent.Execution.Implicits._
import scala.collection.mutable
import com.ning.http.client.Realm.AuthScheme
import play.api.Logger
import play.api.libs.json.{JsValue, JsArray}
import play.api.libs.concurrent.Akka
import scala.collection.mutable.ListBuffer

case class GithubRepository(owner: String, name: String)

object GithubActor {

  val githubApiUrl = "https://api.github.com"

  import play.api.Play

  val login = Play.current.configuration.getString("github.login").get
  val password = Play.current.configuration.getString("github.password").get

}

class GithubActor extends Actor {

  import play.api.Play.current

  val redisActor = Akka.system.actorOf(Props[RedisActor])

  val repoName = new StringBuffer()
  val repoOwner = new StringBuffer()

  val issues = new ListBuffer[JsValue]()

  override def receive: Receive = {

    case repo: GithubRepository =>
      Logger.debug(s"GithubActor | Next Repo : ${repo.owner}/${repo.name}")

      repoName append repo.name
      repoOwner append repo.owner

      getIssues(repo.owner, repo.name)
        .map {
        response =>
          handleGithubResponse(response)
      }

    case link: String =>
      Logger.debug(s"GithubActor | Next call : $link")

      WS.url(link)
        .withAuth(GithubActor.login, GithubActor.password, AuthScheme.BASIC)
        .get()
        .map {
        response =>
          handleGithubResponse(response)
      }

    case error: Exception =>
      Logger.error(s"GithubActor | ERROR : ${error.getMessage}")
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
    WS.url(GithubActor.githubApiUrl + s"/repos/$owner/$repo/issues").withQueryString(
      "state" -> "all",
      "sort" -> "created",
      "direction" -> "asc"
    ).withAuth(GithubActor.login, GithubActor.password, AuthScheme.BASIC).get()
  }

  /**
   *
   * @param response
   */
  private def handleGithubResponse(response: Response) {
    issues ++= response.json.asInstanceOf[JsArray].value

    parseLinkHeader(response.header("Link").get).get("next") match {
      case nextLink: Some[String] =>
        self ! nextLink.get
      case _ =>
        self ! cacheIssues()
    }
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

  /**
   * Ask to the Redis actor to persist the issues
   */
  private def cacheIssues() {
    redisActor ! RedisRepository(repoName.toString, repoOwner.toString, issues.toList)
  }

}
