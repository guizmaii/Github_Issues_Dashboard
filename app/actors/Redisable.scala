package actors

import play.api.libs.concurrent.Akka
import akka.actor.Props


trait Redisable {

  import play.api.Play.current

  val redisActor = Akka.system.actorOf(Props[RedisActor])

}
