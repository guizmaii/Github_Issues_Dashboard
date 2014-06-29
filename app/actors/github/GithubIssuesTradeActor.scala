package actors.github

import actors.compute.G1.G1Actor
import actors.compute.G2.G2Actor
import actors.compute.G4.G4Actor
import akka.actor._
import models.GithubRepository
import play.api.libs.json.JsObject
import spray.client.pipelining._
import spray.http._

import scala.concurrent.Future
import scala.util.{Failure, Success}

// TODO 1 : gérer les headers rate-limits : https://developer.github.com/v3/#rate-limiting
// TODO 1.1 : Les rates limites peuvent être géré avec ça : https://developer.github.com/v3/rate_limit/
// TODO 2 (maybe) : Utiliser les conditional request pour baisser le nombre de requests nécessaire : https://developer.github.com/v3/#conditional-requests

case class RepositoryData(repo: GithubRepository, issues: List[JsObject])

case class CalculationFinishedEvent()

class GithubIssuesTradeActor extends AbstractGithubActor {

  import system.dispatcher // execution context for futures

  override protected val calculatorsList = List(
    context.actorOf(Props[G1Actor], "G1_Actor"),
    context.actorOf(Props[G2Actor], "G2_Actor"),
    context.actorOf(Props[G4Actor], "G4_Actor")
  )

  private var nbCalculator = calculatorsList.size

  var begin = 0L
  var end = 0L

  override def receive: Receive = {

    case repo: GithubRepository =>
      log.debug(s"Next Repo : ${AbstractGithubActor.githubApiUrl}/${repo.owner}/${repo.name}")
      begin = System.currentTimeMillis()

      this.repository = repo

      getIssues(repo.owner, repo.name) onComplete {
        case Success(response) =>
          handleSuccessResponse(response)

        case Failure(error) =>
          handleFailureResponse(error)
          context.stop(self)
      }

    case tuple: (Int, List[JsObject]) =>
      childrenResponses = childrenResponses + tuple
      if (allGettersAnswered) {
        sendDataToCalculators()
      }

    case cfe: CalculationFinishedEvent =>
      nbCalculator -= 1
      if (allCalculatorsHaveFinished) {
        end = System.currentTimeMillis()
        log.debug("Temps de total (récupération des données + calcul) : " + ((end - begin) / 1000) + " secondes")
        context.stop(self)
      }

  }

  def allCalculatorsHaveFinished: Boolean = {
    nbCalculator == 0
  }

  /**
   * List issues for a repository
   *
   * GET /repos/:owner/:repo/issues
   *
   * @param owner
   * @param repo
   * @return
   */
  private def getIssues(owner: String, repo: String): Future[HttpResponse] = {
    pipeline(
      Get(
        s"${AbstractGithubActor.githubApiUrl}/repos/$owner/$repo/issues" +
          s"?client_id=${AbstractGithubActor.client_id}" +
          s"&client_secret=${AbstractGithubActor.client_secret}" +
          s"&per_page=${100}" +
          s"&state=${"all"}" +
          s"&sort=${"created"}" +
          s"&direction=${"asc"}"
      )
    )
  }

}
