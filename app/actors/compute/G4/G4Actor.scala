package actors.compute.G4

import akka.actor.Actor
import actors.RepositoryData
import traits.AsyncRedisable


class G4Actor extends Actor with AsyncRedisable {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
