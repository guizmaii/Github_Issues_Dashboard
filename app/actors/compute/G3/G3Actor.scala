package actors.compute.G3

import actors.github.RepositoryData
import akka.actor.Actor


class G3Actor extends Actor {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
