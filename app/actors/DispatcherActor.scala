package actors

import akka.actor.{Actor, ActorLogging, Props}
import models.GithubRepositoryDAO
import play.api.db.slick._
import play.api.libs.concurrent.Akka

case class LaunchOrder()

class DispatcherActor extends Actor with ActorLogging {

  import play.api.Play.current

  override def receive: Receive = {

    case LaunchOrder =>
      DB.withSession(
       implicit session =>
         GithubRepositoryDAO.getAll
      ) map {
        repo =>
          Akka.system.actorOf(Props[GithubActor], s"${repo.owner}_${repo.name}_actor") ! repo
      }

  }

}
