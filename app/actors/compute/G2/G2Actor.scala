package actors.compute.G2

import actors.github.RepositoryData
import akka.actor.Actor


class G2Actor extends Actor {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
