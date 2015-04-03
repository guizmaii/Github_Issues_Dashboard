package controllers

import models.GithubRepositoryDAO
import play.api.Routes
import play.api.db.slick._
import play.api.mvc._

import scala.concurrent.Future

class Application extends Controller {

  //  TODO : Tester à l'ajout du dépot dans le system si celui-ci ne gére pas les issues

  import play.api.Play.current

  def index = Action.async { implicit request =>
    DB withSession { implicit session =>
      Future.successful(Ok(views.html.index(GithubRepositoryDAO.getAllNonAlreadyFetched)))
    }
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
