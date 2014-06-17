package actors.compute.G3

import akka.actor.Actor
import actors.RepositoryData
import traits.AsyncRedisClient


class G3Actor extends Actor with AsyncRedisClient {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
