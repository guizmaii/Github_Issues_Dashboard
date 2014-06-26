package services

import actors.DispatcherActor
import akka.actor.{ActorRef, Props}
import play.api.libs.concurrent.Akka

object TheGreatDispatcherSingleton {

  import play.api.Play.current

  val instance: ActorRef = Akka.system.actorOf(Props[DispatcherActor], "TheGreatDispatcher")

}
