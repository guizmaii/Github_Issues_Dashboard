package actors.compute.G3

import actors.github.RepositoryData
import akka.actor.Actor
import models.GithubRepository

case class G3ComputedData(repo: GithubRepository) // + ?

class G3Actor extends Actor {

  override def receive: Receive = {

    case data: RepositoryData =>


  }

}
