package controllers

import models.GithubRepositoryDAO
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import play.api.db.slick._
import play.api.mvc._
import services.{GithubRepositoryUrlService, TheGreatDispatcher}
import views.html

case class UserRepoUrl(url: String)

object Config extends Controller {

  import play.api.Play.current

  val repoForm: Form[UserRepoUrl] = Form(
    mapping(
      "url" -> (nonEmptyText verifying pattern(GithubRepositoryUrlService.regexValidator))
    )(UserRepoUrl.apply)(UserRepoUrl.unapply) verifying(
      "Dépot déjà suivi",
      repo =>
        DB.withSession {
          implicit session =>
            GithubRepositoryDAO.notExists(GithubRepositoryUrlService.parseUrl(repo.url))
        }
    )
  )

  implicit val implicitRepoForm = repoForm

  def create = DBAction {
    implicit rs =>
      Ok(html.configuration(GithubRepositoryDAO.getAll))
  }

  def save = DBAction {
    implicit rs =>
      repoForm.bindFromRequest.fold(
        formWithErrors => Redirect(routes.Config.create()).flashing("failure" -> formWithErrors.errors(0).message),
        repoUrl => {
          val githubRepo = GithubRepositoryUrlService.parseUrl(repoUrl.url)
          GithubRepositoryDAO.insert(githubRepo)
          TheGreatDispatcher.getInstance ! githubRepo
          Redirect(routes.Config.create()).flashing("success" -> "Dépôt ajouté avec succés")
        }
      )
  }

  def delete(id: Long) = DBAction {
    implicit rs =>
      GithubRepositoryDAO.delete(id)
      Redirect(routes.Config.create()).flashing("success" -> "Dépôt supprimé avec succées")
  }
}
