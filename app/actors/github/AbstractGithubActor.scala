package actors.github

import akka.actor.{Actor, ActorLogging}
import play.api.libs.json.{JsArray, JsObject, Json}
import spray.http.HttpResponse

abstract class AbstractGithubActor extends Actor with ActorLogging {

  protected def handleOkResponse(response: HttpResponse): Unit

  protected def handleGithubResponse(response: HttpResponse) {
    response.status.isSuccess match {
      case true =>
        handleOkResponse(response)
      case false =>
        handleErrorResponse(response)
        context.stop(self)
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
  protected def handleErrorResponse(response: HttpResponse) = {
    log.error(s"Erreur Github : ${response.status.value}")
  }

  protected def getPageIndexFromLink(link: String): Int = {
    link.split("&page=")(1).toInt
  }

  protected def convertResponseToJsObjectList(response: HttpResponse): List[JsObject] = {
    Json.parse(response.entity.asString).asInstanceOf[JsArray].value.asInstanceOf[Seq[JsObject]].toList
  }

}
