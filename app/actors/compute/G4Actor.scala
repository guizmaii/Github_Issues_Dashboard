package actors.compute

import akka.actor.{PoisonPill, Actor}
import actors.{RepositoryData, Redisable}
import play.api.Logger


class G4Actor extends Actor with Redisable {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      // TODO : Valider l'utiliter de s'envoyer une PoisonPill
      self ! PoisonPill
      throw error
  }

}
