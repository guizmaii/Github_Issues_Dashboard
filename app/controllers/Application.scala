package controllers

import play.api.mvc._

object Application extends Controller {

  //  TODO : Tester à l'ajout du dépot dans le system si celui-ci gére des issues

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index = Action {
    Ok("Welcome Home Boy !\nLook at the console, the actors has been launched and they work for you !")
  }

}
