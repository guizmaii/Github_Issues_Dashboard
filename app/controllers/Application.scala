package controllers

import play.api.Routes
import play.api.mvc._

object Application extends Controller {

  //  TODO : Tester à l'ajout du dépot dans le system si celui-ci gére des issues

  def index = Action {
    implicit rs =>
      Ok(views.html.index())
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
