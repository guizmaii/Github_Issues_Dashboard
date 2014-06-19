package services

import actors.DispatcherActor
import akka.actor.{ActorRef, Props}
import play.api.libs.concurrent.Akka

object TheGreatDispatcher {

  import play.api.Play.current

  private val instance: ActorRef = Akka.system.actorOf(Props[DispatcherActor], "TheGreatDispatcher")

  def getInstance: ActorRef = {
    instance
  }

}
