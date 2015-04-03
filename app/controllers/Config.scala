package controllers

import actors.DeleteOrder
import helpers.GithubRepositoryUrlService
import models.GithubRepositoryDAO
import play.api.data.Forms._
import play.api.data._
import play.api.data.validation.Constraints._
import play.api.db.slick._
import play.api.mvc.{Action, Controller}
import redis.RedisActorSingleton
import services.TheGreatDispatcherSingleton
import views.html

import scala.concurrent.Future

case class UserRepoUrl(url: String)

class Config extends Controller {

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

  def create = Action.async { implicit request =>
    DB.withSession(implicit session => {
      Future.successful(Ok(html.configuration(GithubRepositoryDAO.getAll)))
    })
  }

  def save = Action.async { implicit request =>
    repoForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(Redirect(routes.Config.create()).flashing("failure" -> formWithErrors.errors(0).message)),
      repoUrl => DB.withSession( implicit session => {
        val githubRepo = GithubRepositoryUrlService.parseUrl(repoUrl.url)
        GithubRepositoryDAO.insert(githubRepo)
        TheGreatDispatcherSingleton.instance ! githubRepo
        Future.successful(Redirect(routes.Config.create()).flashing("success" -> "Dépôt ajouté avec succés"))
      })
    )
  }

  def delete(id: Long) = Action.async { implicit request =>
    DB.withSession( implicit session => {
      // Attention l'ordre des opérations est imporant ici
      val repo = GithubRepositoryDAO.get(id)
      GithubRepositoryDAO.delete(id)
      RedisActorSingleton.instance ! DeleteOrder( repo )
      Future.successful(Redirect(routes.Config.create()).flashing("success" -> "Dépôt supprimé avec succées"))
    })
  }
}
