package actors.compute.G2

import akka.actor.Actor
import actors.RepositoryData
import traits.AsyncRedisClient


class G2Actor extends Actor with AsyncRedisClient {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
