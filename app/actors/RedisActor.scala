package actors

import akka.actor.Actor
import com.redis._
import play.api.libs.json.JsArray
import play.api.Logger

object RedisActor {

  lazy val host = "localhost"

  lazy val port = 6379

}

class RedisActor extends Actor {

  val client = new RedisClient(RedisActor.host, RedisActor.port)

  override def receive: Receive = {

    case data: JsArray => {

    }

    case error =>
      Logger.error("ERREUR : " + error)
  }
}
