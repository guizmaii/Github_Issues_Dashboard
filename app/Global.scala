import actors.{DispatcherActor, LaunchOrder}
import akka.actor.{ActorRef, Props}
import play.api._
import play.api.libs.concurrent.Akka

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")

    import play.api.Play.current
    import play.api.libs.concurrent.Execution.Implicits._
    import scala.concurrent.duration._

    val greatArchitect: ActorRef = Akka.system.actorOf(Props[DispatcherActor], "GreatArchitect")

    // Lance la Matrix d'actualisation du cache des données pour les graphs toutes les heures,
    // à partir de maintenant.
    Akka.system.scheduler.schedule(0.microsecond, 1.hour, greatArchitect, LaunchOrder)
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
