package traits

import play.api.libs.concurrent.Akka
import akka.actor.Props
import actors.RedisActor


trait AsyncRedisable {

  import play.api.Play.current

  val redisActor = Akka.system.actorOf(Props[RedisActor])

}
