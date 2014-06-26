package redis

import actors.RedisActor
import akka.actor.Props
import play.api.libs.concurrent.Akka


object RedisActorSingleton {

  import play.api.Play.current

  val instance = Akka.system.actorOf(Props[RedisActor], "RedisActorSingleton")

}
