package actors.github

import play.api.libs.concurrent.Akka
import spray.client.pipelining._
import spray.http._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class GithubSingleGetterActor extends AbstractGithubActor {

  import play.api.Play.current

  // Needed by spray-client
  implicit val system = Akka.system
  import system.dispatcher // execution context for futures

  val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  private val selfId: Int = self.path.name.split("_")(1).toInt

  override def receive: Receive = {

    case link: String =>
      log.debug(s"Next link : ${getPageIndexFromLink(link)}")

      pipeline(Get(link)) onComplete {
        case Success(response) =>
          sender ! (selfId -> convertResponseToJsObjectList(response))

        case Failure(error) =>
          handleFailureResponse(error)
          context.stop(self)
      }

  }

}
