package actors

import play.api.Logger
import akka.actor.{PoisonPill, Props, Actor}
import play.api.libs.concurrent.Akka
import actors.compute.{G4Actor, G3Actor, G2Actor, G1Actor}
import domain._

import scala.collection.mutable.ListBuffer

import play.api.libs.json._// JSON library

case class ParsedRepositoryData(repo: GithubRepository, parsedData: List[GithubIssue])

object IssueParserActor {

  import play.api.Play.current
  // TODO : Réfléchir : est-il mieux de ne créer que 4 acteurs de calculs ou 4 par dépot ?
  var g1Calculator = Akka.system.actorOf(Props[G1Actor])
  var g2Calculator = Akka.system.actorOf(Props[G2Actor])
  var g3Calculator = Akka.system.actorOf(Props[G3Actor])
  var g4Calculator = Akka.system.actorOf(Props[G4Actor])

}

class IssueParserActor extends Actor {

  val issues = new ListBuffer[GithubIssue]()

  override def receive: Receive = {

    // TODO FIX!! : Le parsage n'a pas l'air de fonctionner !
    case data: RepositoryData =>
      data.issues map {
        jsonIssue =>
          jsonIssue.validate[GithubIssue] match {
            case s: JsSuccess[GithubIssue] =>
              issues += s.get
            case e: JsError =>
              Logger.error(s"${this.getClass} | ERROR : $e")
          }
      }
      val parsedRepo = ParsedRepositoryData(data.repo, issues.toList)
      IssueParserActor.g1Calculator ! parsedRepo
      IssueParserActor.g2Calculator ! parsedRepo
      IssueParserActor.g3Calculator ! parsedRepo
      IssueParserActor.g4Calculator ! parsedRepo
      self ! PoisonPill

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      // TODO : Valider l'utiliter de s'envoyer une PoisonPill
      self ! PoisonPill
      throw error
  }

}
