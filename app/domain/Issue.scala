package domain

import org.joda.time.DateTime

import play.api.libs.json._
import play.api.libs.functional.syntax._

// TODO : Faire une librairie externe pour tout ce qui concerne Github et qui peut s'avérer réutilisable

case class GithubIssue(url: String,
                       labels_url: String,
                       comments_url: String,
                       events_url: String,
                       html_url: String,
                       id: Int,
                       number: Int,
                       title: String,
                       body: String,
                       user: GithubUser,
                       labels: Seq[GithubLabel],
                       state: String,
                       assignee: GithubUser,
                       milestone: GithubMilestone,
                       comments: Int,
                       pull_request: GithubPullRequest,
                       closed_at: String,
                       created_at: String,
                       updated_at: String)
{
  def isNotClosed: Boolean = closed_at.isEmpty
  def isCreatedBefore(date: DateTime): Boolean = DateTime.parse(created_at).isBefore(date)
  def isClosedAfter(date: DateTime): Boolean = DateTime.parse(closed_at).isAfter(date)
  def isOpenAtThisDate(date: DateTime): Boolean = isCreatedBefore(date) && (isNotClosed || isClosedAfter(date))
}

case object GithubIssue {
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
}

case class GithubUser(login: String,
                      id: Int,
                      avatar_url: String,
                      gravatar_id: String,
                      url: String,
                      html_url: String,
                      followers_url: String,
                      following_url: String,
                      gists_url: String,
                      starred_url: String,
                      subscriptions_url: String,
                      organizations_url: String,
                      repos_url: String,
                      events_url: String,
                      received_events_url: String,
                      user_type: String,
                      site_admin: Boolean)

case object GithubUser {
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
}

case class GithubLabel(url: String, name: String, color: String)

case object GithubLabel {
  implicit val GithubLabelReads: Format[GithubLabel] = (
    (JsPath \ "url").format[String] and
      (JsPath \ "name").format[String] and
      (JsPath \ "color").format[String]
    )(GithubLabel.apply, unlift(GithubLabel.unapply))
}

case class GithubMilestone(url: String,
                           number: Int,
                           state: String,
                           title: String,
                           description: String,
                           creator: GithubUser,
                           open_issues: Int,
                           closed_issues: Int,
                           created_at: String,
                           updated_at: String,
                           due_on: Boolean)

case object GithubMilestone {
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
}

case class GithubPullRequest(url: String, html_url: String, diff_url: String, patch_url: String)

case object GithubPullRequest {
  implicit val GithubPullRequestReads: Format[GithubPullRequest] = (
    (JsPath \ "url").format[String] and
      (JsPath \ "html_url").format[String] and
      (JsPath \ "diff_url").format[String] and
      (JsPath \ "patch_url").format[String]
    )(GithubPullRequest.apply, unlift(GithubPullRequest.unapply))
}
