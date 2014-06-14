package actors.compute.G2

import akka.actor.Actor
import actors.RepositoryData
import traits.AsyncRedisable


class G2Actor extends Actor with AsyncRedisable {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
