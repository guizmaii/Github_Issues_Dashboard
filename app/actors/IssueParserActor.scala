package actors

import play.api.Logger
import akka.actor.{Props, Actor}
import play.api.libs.concurrent.Akka
import actors.compute.{G4Actor, G3Actor, G2Actor, G1Actor}
import domain.GithubIssue

case class ParsedRepositoryData(repoName: String, repoOwner: String, computedData: List[GithubIssue])

class IssueParserActor extends Actor {

  import play.api.Play.current

  var g1Calculator = Akka.system.actorOf(Props[G1Actor])
  var g2Calculator = Akka.system.actorOf(Props[G2Actor])
  var g3Calculator = Akka.system.actorOf(Props[G3Actor])
  var g4Calculator = Akka.system.actorOf(Props[G4Actor])

  override def receive: Receive = {

    case data: RepositoryData => {

    }

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      throw error
  }
}
