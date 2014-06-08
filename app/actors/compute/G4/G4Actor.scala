package actors.compute.G4

import akka.actor.{PoisonPill, Actor}
import actors.{RepositoryData, Redisable}
import play.api.Logger


class G4Actor extends Actor with Redisable {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
