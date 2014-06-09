package actors.compute.G3

import akka.actor.Actor
import actors.RepositoryData
import traits.AsyncRedisable


class G3Actor extends Actor with AsyncRedisable {

  override def receive: Receive = {

    case data: RepositoryData => {

    }

  }

}
