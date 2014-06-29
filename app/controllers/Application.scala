package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import com.mohiva.play.silhouette.core.{LogoutEvent, Environment, Silhouette}
import forms.{SignUpForm, SignInForm}
import models.{GithubRepositoryDAO, User}
import play.api.Routes
import play.api.db.slick._
import play.api.mvc._

import scala.concurrent.Future

class Application @Inject() (implicit val env: Environment[User, CachedCookieAuthenticator])
  extends Silhouette[User, CachedCookieAuthenticator] {

  //  TODO : Tester à l'ajout du dépot dans le system si celui-ci ne gére pas les issues

  import play.api.Play.current

  def index = UserAwareAction.async { implicit request =>
    DB withSession { implicit session =>
      Future.successful(
        Ok(views.html.index(GithubRepositoryDAO.getAllNonAlreadyFetched, request.identity))
      )
    }
  }

  def account = SecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.account(request.identity)))
  }

  /**
   * Handles the Sign In action.
   *
   * @return The result to display.
   */
  def signIn = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.Application.index))
      case None => Future.successful(Ok(views.html.signIn(SignInForm.form)))
    }
  }

  /**
   * Handles the Sign Up action.
   *
   * @return The result to display.
   */
  def signUp = UserAwareAction.async { implicit request =>
    request.identity match {
      case Some(user) => Future.successful(Redirect(routes.Application.index))
      case None => Future.successful(Ok(views.html.signUp(SignUpForm.form)))
    }
  }

  /**
   * Handles the Sign Out action.
   *
   * @return The result to display.
   */
  def signOut = SecuredAction.async { implicit request =>
    env.eventBus.publish(LogoutEvent(request.identity, request, request2lang))
    Future.successful(env.authenticatorService.discard(Redirect(routes.Application.index)))
  }

  // -- Javascript routing

  def javascriptRoutes = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.G1Controller.getAll,
        routes.javascript.G2Controller.getAll,
        routes.javascript.G3Controller.getAll,
        routes.javascript.G4Controller.getAll
      )
    ).as("text/javascript")
  }

}
