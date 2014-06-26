package controllers

import play.api.libs.json._
import play.api.mvc._
import redis.Redis
import spray.json._

case class G4Json(label: String, value: Int)

object G4JsonProtocol extends DefaultJsonProtocol {
  implicit val g4Format = jsonFormat2(G4Json)
}

object G4Controller extends Controller {

  import controllers.G4JsonProtocol._

  def getAll = Action {
    val data: List[G4Json] = (Redis.g4GetAll map { t => G4Json(t._1, t._2) }).toList
    Ok(Json.parse(data.toJson.compactPrint))
  }

}
