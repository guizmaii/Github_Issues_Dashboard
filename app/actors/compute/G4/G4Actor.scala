package actors.compute.G4

import actors.RepositoryData
import akka.actor.Actor
import traits.AsyncRedisClient


class G4Actor extends Actor with AsyncRedisClient {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
