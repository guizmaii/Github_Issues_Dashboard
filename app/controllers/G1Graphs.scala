package controllers

import play.api.mvc._
import traits.SyncRedisable
import domain.G1Type
import play.api.libs.json.Json


object G1Graphs extends Controller with SyncRedisable {

  import com.redis.serialization._
  import com.redis.serialization.Parse.Implicits._

  def getAll = Action {
    val data = redisPool.withClient {
      client =>
        client.hgetall[String, Int](getRedisKey("guizmaii", "Github_Issues_Dashboard", G1Type))
    }
    Ok(Json.toJson(data.get))
  }

}
