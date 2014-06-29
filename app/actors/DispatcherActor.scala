package actors

import actors.github.{GithubEventsTradeActor, GithubIssuesTradeActor}
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
    context.actorOf(Props[GithubIssuesTradeActor], s"${repo.owner}_${repo.name}_issues_actor") ! repo
    context.actorOf(Props[GithubEventsTradeActor], s"${repo.owner}_${repo.name}_events_actor") ! repo
  }

}
