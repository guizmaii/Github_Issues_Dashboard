package controllers

import models.{GithubRepository, GithubRepositoryDAO}
import play.api.db.slick._
import play.api.libs.json._
import play.api.mvc._
import redis.G1Redis
import spray.json._

import scala.collection.immutable.TreeMap

case class G1Json(key: String, values: Array[Array[Long]])

object G1JsonProtocol extends DefaultJsonProtocol {
  implicit val g1Format = jsonFormat2(G1Json)
}

object G1Controller extends Controller {

  import controllers.G1JsonProtocol._

  def getAll = DBAction {
    implicit rs =>
      val data = GithubRepositoryDAO.getAll map {
        repo: GithubRepository =>
          G1Json( repo.name, getFormatedDataForJS(G1Redis.get(repo)) )
      }
      // TODO : Find a way to not parse twice to JSON.
      Ok(Json.parse(data.toJson.compactPrint))
  }

  private def getFormatedDataForJS(data : Map[Long, Int]): Array[Array[Long]] = {
    MapToArrayOfArrayOfLong(
      sortData(
        data
      )
    )
  }

  private def sortData(data: Map[Long, Int]): TreeMap[Long, Int] = {
    TreeMap[Long, Int]() ++ ( for(t <- data) yield t._1 -> t._2 )
  }

  private def MapToArrayOfArrayOfLong(data: Map[Long, Int]): Array[Array[Long]] = {
    data.toArray map {
      t =>
        Array(t._1, t._2)
    }
  }

}
