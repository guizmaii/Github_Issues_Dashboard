package actors.github

import akka.actor.{Actor, ActorLogging}
import play.api.libs.json.{JsArray, JsObject, Json}
import spray.http.HttpResponse

abstract class AbstractGithubActor extends Actor with ActorLogging {

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

  protected def getPageIndexFromLink(link: String): Int = {
    link.split("&page=")(1).toInt
  }

  protected def convertResponseToJsObjectList(response: HttpResponse): List[JsObject] = {
    Json.parse(response.entity.asString).asInstanceOf[JsArray].value.asInstanceOf[Seq[JsObject]].toList
  }

}
