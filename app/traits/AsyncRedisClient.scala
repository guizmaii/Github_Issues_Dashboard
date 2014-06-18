package traits

import actors.RedisActor
import akka.actor.Props
import play.api.libs.concurrent.Akka


trait AsyncRedisClient {

  import play.api.Play.current

  val redisActor = Akka.system.actorOf(Props[RedisActor], "RedisClient")

}
