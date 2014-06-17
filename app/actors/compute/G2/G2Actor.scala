package actors.compute.G2

import actors.RepositoryData
import akka.actor.Actor
import traits.AsyncRedisClient


class G2Actor extends Actor with AsyncRedisClient {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
