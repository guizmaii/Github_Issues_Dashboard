package actors.compute.G3

import akka.actor.{PoisonPill, Actor}
import actors.{RepositoryData, Redisable}
import play.api.Logger


class G3Actor extends Actor with Redisable {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
