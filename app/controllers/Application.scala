package controllers

import play.api.mvc._
import play.api.Routes

object Application extends Controller {

  //  TODO : Tester à l'ajout du dépot dans le system si celui-ci gére des issues

  def index = Action {
    Ok("Welcome Home Boy !\nLook at the console, the actors has been launched and they work for you !")
  }

  // -- Javascript routing

  def javascriptRoutes = Action { implicit request =>
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        G1Graphs.getAll,
        G2Graphs.getAll,
        G3Graphs.getAll,
        G4Graphs.getAll
      )
    ).as("text/javascript")
  }

}
