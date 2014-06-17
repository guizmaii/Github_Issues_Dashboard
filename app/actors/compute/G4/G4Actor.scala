package actors.compute.G4

import akka.actor.Actor
import actors.RepositoryData
import traits.AsyncRedisClient


class G4Actor extends Actor with AsyncRedisClient {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
