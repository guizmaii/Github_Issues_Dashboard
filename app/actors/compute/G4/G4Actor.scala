package actors.compute.G4

import actors.github.RepositoryData
import akka.actor.Actor


class G4Actor extends Actor {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
