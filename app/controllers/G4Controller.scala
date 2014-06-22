package controllers

import domain.G4Type
import play.api.libs.json._
import play.api.mvc._
import traits.SyncRedisClient
import spray.json._

case class G4Json(label: String, value: Int)

object G4JsonProtocol extends DefaultJsonProtocol {
  implicit val g4Format = jsonFormat2(G4Json)
}

object G4Controller extends Controller with SyncRedisClient {

  import com.redis.serialization.Parse.Implicits._
  import controllers.G4JsonProtocol._

  def getAll = Action {

    val data: List[G4Json] = (redisPool.withClient {
      client =>
        client.hgetall[String, Int](G4key).get
    } map {
      t =>
        G4Json(t._1, t._2)
    }).toList

    Ok(Json.parse(data.toJson.compactPrint))
  }

}
