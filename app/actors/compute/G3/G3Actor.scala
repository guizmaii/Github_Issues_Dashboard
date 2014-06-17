package actors.compute.G3

import actors.RepositoryData
import akka.actor.Actor
import traits.AsyncRedisClient


class G3Actor extends Actor with AsyncRedisClient {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
