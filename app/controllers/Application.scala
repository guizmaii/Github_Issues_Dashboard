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
        routes.javascript.G1Graphs.getAll,
        routes.javascript.G2Graphs.getAll,
        routes.javascript.G3Graphs.getAll,
        routes.javascript.G4Graphs.getAll
      )
    ).as("text/javascript")
  }

}
