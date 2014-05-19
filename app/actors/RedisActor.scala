package actors

import akka.actor.Actor
import com.redis._
import play.api.libs.json.JsValue
import play.api.Logger

case class RedisRepository(repoName: String, repoOwner: String, issues: List[JsValue])

object RedisActor {

  import play.api.Play

  val host: String = Play.current.configuration.getString("redis.host").get
  val port: Int = Integer.parseInt(Play.current.configuration.getString("redis.port").get)

}

class RedisActor extends Actor {

  val client = new RedisClient(RedisActor.host, RedisActor.port)

  override def receive: Receive = {

    case repo: RedisRepository =>
      // TODO : Coder l'enregistrement des données

    case error =>
      Logger.error("ERREUR : " + error)
  }
}
