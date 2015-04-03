import actors.StartOrder
import play.api._
import play.api.libs.concurrent.Akka
import services.TheGreatDispatcherSingleton

import scala.concurrent.duration._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    super.onStart(app)
    Logger.info("Application has started")

    import play.api.Play.current
    import play.api.libs.concurrent.Execution.Implicits._

    // Lance la Matrix d'actualisation du cache des données pour les graphs toutes les heures,
    // à partir de maintenant.
    Akka.system.scheduler.schedule(0.microsecond, 1.hour, TheGreatDispatcherSingleton.instance, StartOrder)
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
