package actors.github

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import models.GithubRepository
import play.api.libs.concurrent.Akka
import play.api.libs.json.{JsArray, JsObject, Json}
import spray.client.pipelining._
import spray.http._

import scala.collection.immutable.TreeMap
import scala.concurrent.Future

object AbstractGithubActor {

  val githubApiUrl = "https://api.github.com"

  import play.api.Play

  val client_id = Play.current.configuration.getString("github.client.id").get
  val client_secret = Play.current.configuration.getString("github.client.secret").get

}

abstract class AbstractGithubActor extends Actor with ActorLogging {

  import play.api.Play.current

  // Needed by spray-client
  implicit val system = Akka.system
  import system.dispatcher // execution context for futures
  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  protected var repository: GithubRepository = null
  protected val calculatorsList: List[ActorRef] = null

  protected var childrenResponses = TreeMap[Int, List[JsObject]]()

  protected var nbPage = 0

  // TODO : Améliorer la gestion des réponses non 200
  /**
   * Documentation des erreurs de l'API Github :
   *
   * https://developer.github.com/v3/#client-errors
   *
   * @param error
   */
  protected def handleFailureResponse(error: Throwable) = {
    log.error(s"Erreur Github : ${error.getMessage}")
  }

  protected def handleSuccessResponse(response: HttpResponse): Unit = {
    childrenResponses = childrenResponses + (1 -> convertResponseToJsObjectList(response))

    // TODO : Refactor cet algo. Bcp trop compliqué par rapport à ce qu'il fait.
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

  protected def allGettersAnswered: Boolean = {
    childrenResponses.size == nbPage
  }

  protected def sendDataToCalculators(): Unit = {
    val data = RepositoryData(repository, childrenResponses.map(_._2).flatten.toList)
    calculatorsList map { _ ! data }
  }

  protected def constructLink(link: String, pageIndex: Int): String = {
    s"${link.split("&page=")(0)}&page=$pageIndex".trim
  }

  protected def getPageIndexFromLink(link: String): Int = {
    link.split("&page=")(1).toInt
  }

  protected def convertResponseToJsObjectList(response: HttpResponse): List[JsObject] = {
    Json.parse(response.entity.asString).asInstanceOf[JsArray].value.asInstanceOf[Seq[JsObject]].toList
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
  protected def parseLinkHeader(linkHeader: String): Map[String, String] = {
    (linkHeader.split(',') map {
      part =>
        val section = part.split(';')
        val url = section(0).replace("<", "").replace(">", "")
        val name = section(1).replace(" rel=", "")
        (name, url)
    }).toMap
  }



}
