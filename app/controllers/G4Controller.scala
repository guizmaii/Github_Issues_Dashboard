package controllers

import models.{GithubRepositoryDAO, GithubRepository}
import play.api.db.slick._
import play.api.libs.json._
import play.api.mvc._
import redis.G4Redis
import spray.json._

case class G4Json(label: String, value: Int)

object G4JsonProtocol extends DefaultJsonProtocol {
  implicit val g4Format = jsonFormat2(G4Json)
}

object G4Controller extends Controller {

  import controllers.G4JsonProtocol._

  def getAll = DBAction {
    implicit rs =>
      val data: List[G4Json] = getFormatedForJs( G4Redis getAll GithubRepositoryDAO.getAll )
      Ok(Json.parse(data.toJson.compactPrint))
  }

  private def getFormatedForJs(mapList: List[Map[String, Int]]): List[G4Json] = {
    (aggregateStatesAndOccurrences(mapList) map {
      tuple =>
        G4Json(tuple._1, tuple._2)
    }).toList
  }

  private def aggregateStatesAndOccurrences(mapList: List[Map[String, Int]]): Map[String, Int] = {
    var res = Map[String, Int]()
    mapList map {
      states =>
        states map {
          tuple =>
            res = res + (tuple._1 -> (res.getOrElse(tuple._1, 0) + tuple._2.asInstanceOf[Int]))
        }
    }
    res
  }


}
