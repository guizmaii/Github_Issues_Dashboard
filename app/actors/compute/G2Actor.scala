package actors.compute

import akka.actor.Actor
import actors.{RepositoryData, Redisable}
import play.api.Logger


class G2Actor extends Actor with Redisable {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      throw error
  }

}