package controllers

import models.RepoDAO
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.db.slick._
import play.api.mvc._
import services.GithubRepositoryUrlService
import views.html

case class RepoUrl(url: String)

object Repo extends Controller {

  import play.api.Play.current

  val repoForm = Form(
    mapping(
      "url" -> (nonEmptyText verifying pattern(GithubRepositoryUrlService.regexValidator))
    )(RepoUrl.apply)(RepoUrl.unapply)
  )

  implicit val implicitRepoForm = repoForm

  def create = Action {
    implicit rs =>
      DB.withSession {
        implicit session =>
          Ok(html.configuration(RepoDAO.getAll))
      }
  }

  def save = DBAction {
    implicit rs =>
      repoForm.bindFromRequest.fold(
        formWithErrors => Redirect(routes.Repo.create()).flashing("failure" -> formWithErrors.errors(0).message),
        repoUrl => {
          RepoDAO.save(GithubRepositoryUrlService.parseUrl(repoUrl.url))
          Redirect(routes.Repo.create()).flashing("success" -> "Repo ajouter avec succés")
        }
      )
  }

  def delete(id: Long) = DBAction {
    implicit rs =>
      RepoDAO.delete(id)
      Redirect(routes.Repo.create()).flashing("success" -> "Repo supprimer avec succées")
  }
}
