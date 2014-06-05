package actors.compute

import akka.actor.{PoisonPill, Actor}
import actors.{ParsedRepositoryData, Redisable}
import play.api.Logger


class G1Actor extends Actor with Redisable {

  override def receive: Receive = {

    case data: ParsedRepositoryData => {

    }

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      // TODO : Valider l'utiliter de s'envoyer une PoisonPill
      self ! PoisonPill
      throw error
  }

}
