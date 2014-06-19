package controllers

import domain.G1Type
import play.api.libs.json._
import play.api.mvc._
import spray.json._
import traits.SyncRedisClient

import scala.collection.immutable.TreeMap

case class G1Json(key: String, values: Array[Array[Long]])

object G1JsonProtocol extends DefaultJsonProtocol {
  implicit val colorFormat = jsonFormat2(G1Json)
}

object G1Graphs extends Controller with SyncRedisClient {

  import com.redis.serialization.Parse.Implicits._

  def getAll = Action {
    val data = redisPool.withClient {
      client =>
        client.hgetall[Long, Int](getRedisKey("scala", "scala", G1Type))
    }

    val sortedAndFormatedData = MapToArrayOfArrayOfLong(sortData(data.get))

    import controllers.G1JsonProtocol._

    // TODO : Find a way to not parse twice to JSON.
    Ok(Json.parse(List(G1Json("Github_Issues_Dashboard", sortedAndFormatedData)).toJson.compactPrint))
  }

  private def sortData(data: Map[Long, Int]): TreeMap[Long, Int] = {
    TreeMap[Long, Int]().++( for(t <- data) yield t._1 -> t._2 )
  }

  private def MapToArrayOfArrayOfLong(data: Map[Long, Int]): Array[Array[Long]] = {
    data.toArray map {
      t =>
        Array(t._1, t._2)
    }
  }

}
