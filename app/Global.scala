import _root_.utils.di.SilhouetteModule
import actors.StartOrder
import com.google.inject.{Guice, Injector}
import com.mohiva.play.silhouette.core.{Logger, SecuredSettings}
import controllers.routes
import play.api._
import play.api.i18n.{Messages, Lang}
import play.api.libs.concurrent.Akka
import play.api.mvc.Results._
import play.api.mvc.{Result, RequestHeader}
import services.TheGreatDispatcherSingleton

import scala.concurrent.Future
import scala.concurrent.duration._

object Global extends GlobalSettings with SecuredSettings with Logger {

  /**
   * The Guice dependencies injector.
   */
  var injector: Injector = _

  override def onStart(app: Application) {
    super.onStart(app)
    Logger.info("Application has started")

    // Now the configuration is read and we can create our Injector.
    injector = Guice.createInjector(new SilhouetteModule())

    import play.api.Play.current
    import play.api.libs.concurrent.Execution.Implicits._

    // Lance la Matrix d'actualisation du cache des données pour les graphs toutes les heures,
    // à partir de maintenant.
    Akka.system.scheduler.schedule(0.microsecond, 1.hour, TheGreatDispatcherSingleton.instance, StartOrder)
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

  /**
   * Loads the controller classes with the Guice injector,
   * in order to be able to inject dependencies directly into the controller.
   *
   * @param controllerClass The controller class to instantiate.
   * @return The instance of the controller class.
   * @throws Exception if the controller couldn't be instantiated.
   */
  override def getControllerInstance[A](controllerClass: Class[A]) = injector.getInstance(controllerClass)

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.Application.signIn)))
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.Application.signIn).flashing("error" -> Messages("access.denied"))))
  }

}
