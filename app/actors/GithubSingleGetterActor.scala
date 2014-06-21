package actors

import akka.actor.ActorRef
import play.api.libs.ws.{WS, WSResponse}


class GithubSingleGetterActor extends AbstractGithubActor {

  import play.api.Play.current

import scala.concurrent.ExecutionContext.Implicits._

  private var theSender: ActorRef = null

  override def receive: Receive = {

    case link: String =>
      log.debug(s"Next link : ${getPageIndexFromLink(link)}")

      // Je suis obligé de faire une sauvegarde du sender ici, sinon je perds sa référence
      // et ne peut donc plus l'utiliser dans la méthode handleOkResponse
      theSender = sender()

      WS.url(link)
        .get()
        .map {
        response =>
          handleGithubResponse(response)
      }

  }

  override protected def handleOkResponse(response: WSResponse): Unit = {
    theSender ! (self.path.name.split("_")(1).toInt -> convertResponseToJsObjectList(response))
  }

}
