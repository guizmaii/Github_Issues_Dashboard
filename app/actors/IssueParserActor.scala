package actors

import play.api.Logger
import akka.actor.{PoisonPill, Props, Actor}
import play.api.libs.concurrent.Akka
import actors.compute.{G4Actor, G3Actor, G2Actor, G1Actor}
import domain._

import scala.collection.mutable.ListBuffer

import play.api.libs.json._// JSON library
import play.api.libs.functional.syntax._ // Combinator syntax

case class ParsedRepositoryData(repoName: String, repoOwner: String, computedData: List[GithubIssue])

// TODO : Faire une librairie externe pour tout ce qui concerne Github et qui peut s'avérer réutilisable

object IssueParserActor {

  implicit val GithubIssueReads: Format[GithubIssue] = (
    (JsPath \ "url").format[String] and
    (JsPath \ "labels_url").format[String] and
    (JsPath \ "comments_url").format[String] and
    (JsPath \ "events_url").format[String] and
    (JsPath \ "html_url").format[String] and
    (JsPath \ "id").format[Int] and
    (JsPath \ "number").format[Int] and
    (JsPath \ "title").format[String] and
    (JsPath \ "body").format[String] and
    (JsPath \ "user").format[GithubUser] and
    (JsPath \ "labels").format[Seq[GithubLabel]] and
    (JsPath \ "state").format[String] and
    (JsPath \ "assignee").format[GithubUser] and
    (JsPath \ "milestone").format[GithubMilestone] and
    (JsPath \ "comments").format[Int] and
    (JsPath \ "pull_request").format[GithubPullRequest] and
    (JsPath \ "closed_at").format[String] and
    (JsPath \ "created_at").format[String] and
    (JsPath \ "updated_at").format[String]
  )(GithubIssue.apply, unlift(GithubIssue.unapply))

  implicit val GithubUserReads: Format[GithubUser] = (
    (JsPath \ "login").format[String] and
    (JsPath \ "id").format[Int] and
    (JsPath \ "avatar_url").format[String] and
    (JsPath \ "gravatar_id").format[String] and
    (JsPath \ "url").format[String] and
    (JsPath \ "html_url").format[String] and
    (JsPath \ "followers_url").format[String] and
    (JsPath \ "following_url").format[String] and
    (JsPath \ "gists_url").format[String] and
    (JsPath \ "starred_url").format[String] and
    (JsPath \ "subscriptions_url").format[String] and
    (JsPath \ "organizations_url").format[String] and
    (JsPath \ "repos_url").format[String] and
    (JsPath \ "events_url").format[String] and
    (JsPath \ "received_events_url").format[String] and
    (JsPath \ "user_type").format[String] and
    (JsPath \ "site_admin").format[Boolean]
  )(GithubUser.apply, unlift(GithubUser.unapply))

  implicit val GithubLabelReads: Format[GithubLabel] = (
    (JsPath \ "url").format[String] and
    (JsPath \ "name").format[String] and
    (JsPath \ "color").format[String]
  )(GithubLabel.apply, unlift(GithubLabel.unapply))

  implicit val GithubMilestoneReads: Format[GithubMilestone] = (
    (JsPath \ "url").format[String] and
    (JsPath \ "number").format[Int] and
    (JsPath \ "state").format[String] and
    (JsPath \ "title").format[String] and
    (JsPath \ "description").format[String] and
    (JsPath \ "creator").format[GithubUser] and
    (JsPath \ "open_issues").format[Int] and
    (JsPath \ "closed_issues").format[Int] and
    (JsPath \ "created_at").format[String] and
    (JsPath \ "updated_at").format[String] and
    (JsPath \ "due_on").format[Boolean]
  )(GithubMilestone.apply, unlift(GithubMilestone.unapply))

  implicit val GithubPullRequestReads: Format[GithubPullRequest] = (
    (JsPath \ "url").format[String] and
    (JsPath \ "html_url").format[String] and
    (JsPath \ "diff_url").format[String] and
    (JsPath \ "patch_url").format[String]
  )(GithubPullRequest.apply, unlift(GithubPullRequest.unapply))

}

class IssueParserActor extends Actor {

  import play.api.Play.current

  var g1Calculator = Akka.system.actorOf(Props[G1Actor])
  var g2Calculator = Akka.system.actorOf(Props[G2Actor])
  var g3Calculator = Akka.system.actorOf(Props[G3Actor])
  var g4Calculator = Akka.system.actorOf(Props[G4Actor])

  val issues = new ListBuffer[GithubIssue]()

  override def receive: Receive = {

    case repo: RepositoryData =>
      repo.issues map {
        jsonIssue =>
          jsonIssue.validate[GithubIssue] match {
            case s: JsSuccess[GithubIssue] =>
              issues += s.get
            case e: JsError =>
              Logger.error(s"${this.getClass} | ERROR : $e")
          }
      }
      val parsedRepo = ParsedRepositoryData(repo.owner, repo.name, issues.toList)
      g1Calculator ! parsedRepo
      g2Calculator ! parsedRepo
      g3Calculator ! parsedRepo
      g4Calculator ! parsedRepo
      self ! PoisonPill

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      // TODO : Valider l'utiliter de s'envoyer une PoisonPill
      self ! PoisonPill
      throw error
  }

}
