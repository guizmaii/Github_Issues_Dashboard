package controllers

import play.api.mvc._

object Application extends Controller {

  implicit val context = scala.concurrent.ExecutionContext.Implicits.global

  def index = Action {
    Ok("Welcome Home Boy !\nLook at the console, the actors has been launched and they work for you !")
  }

}
