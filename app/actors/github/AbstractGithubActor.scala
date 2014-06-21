package actors.github

import akka.actor.{Actor, ActorLogging}
import play.api.libs.json.{JsArray, JsObject}
import play.api.libs.ws.WSResponse


abstract class AbstractGithubActor extends Actor with ActorLogging {

  protected def handleOkResponse(response: WSResponse): Unit

  protected def handleGithubResponse(response: WSResponse) {
    response.status match {
      case 200 =>
        handleOkResponse(response)
      case _ =>
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
  protected def handleErrorResponse(response: WSResponse) = {
    log.error(s"Erreur Github : ${response.json \ "message"}")
  }

  protected def getPageIndexFromLink(link: String): Int = {
    link.split("&page=")(1).toInt
  }

  protected def convertResponseToJsObjectList(response: WSResponse): List[JsObject] = {
    response.json.asInstanceOf[JsArray].value.asInstanceOf[Seq[JsObject]].toList
  }

}
