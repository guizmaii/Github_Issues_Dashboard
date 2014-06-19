package services

import actors.RedisActor
import akka.actor.{ActorRef, Props}
import play.api.libs.concurrent.Akka


object RedisClient {

  import play.api.Play.current

  private val instance = Akka.system.actorOf(Props[RedisActor], "RedisClient")

  def getInstance: ActorRef = {
    instance
  }

}
