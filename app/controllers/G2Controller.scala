package controllers

import models.GithubRepositoryDAO
import play.api.db.slick._
import play.api.libs.json.Json
import play.api.mvc._
import redis.G2Redis
import spray.json._

case class G2Value(label: String, value: Long)
case class G2Json(key: String, values: List[G2Value])

object G2JsonProtocol extends DefaultJsonProtocol {
  implicit val g2ValueFormat = jsonFormat2(G2Value)
  implicit val g2Format = jsonFormat2(G2Json)
}

object G2Controller extends Controller {

  import G2JsonProtocol._

  def getAll = DBAction {
    implicit rs =>
      val g2Json = G2Json(
        "Projects velocity",
        GithubRepositoryDAO.getAllFetched map {
          repo =>
            G2Value(repo.name, G2Redis get repo)
        }
      )
      Ok(Json.parse(List(g2Json).toJson.compactPrint))
  }

}
