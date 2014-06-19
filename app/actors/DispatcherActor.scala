package actors

import akka.actor.{Actor, ActorLogging, Props}
import models.{GithubRepository, GithubRepositoryDAO}
import play.api.db.slick._

case class StartOrder()

class DispatcherActor extends Actor with ActorLogging {

  import play.api.Play.current

  override def receive: Receive = {

    case StartOrder =>
      DB.withSession(
       implicit session =>
         GithubRepositoryDAO.getAll
      ) map {
        repo =>
          lauchComputation(repo)
      }

    case repo: GithubRepository =>
      lauchComputation(repo)

  }

  private def lauchComputation(repo: GithubRepository) {
    context.actorOf(Props[GithubTradeActor], s"${repo.owner}_${repo.name}_actor") ! repo
  }

}
