package actors.github

import akka.actor.ActorRef
import spray.client.pipelining._

import scala.util.{Failure, Success}

class GithubSingleGetterActor extends AbstractGithubActor {

  import system.dispatcher // execution context for futures

  private var theSender: ActorRef = null

  private val selfId: Int = self.path.name.split("_")(1).toInt

  override def receive: Receive = {

    case link: String =>
      log.debug(s"Next link : ${getPageIndexFromLink(link)}")

      // Je suis obligé de faire une sauvegarde du sender ici, sinon je perds sa référence
      // et ne peut donc plus l'utiliser dans le onComplete ci-dessous.
      theSender = sender()

      pipeline(Get(link)) onComplete {
        case Success(response) =>
          theSender ! (selfId -> convertResponseToJsObjectList(response))

        case Failure(error) =>
          handleFailureResponse(error)
          context.stop(self)
      }

  }

}
