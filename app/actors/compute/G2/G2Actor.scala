package actors.compute.G2

import akka.actor.{PoisonPill, Actor}
import actors.{RepositoryData, Redisable}
import play.api.Logger


class G2Actor extends Actor with Redisable {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
