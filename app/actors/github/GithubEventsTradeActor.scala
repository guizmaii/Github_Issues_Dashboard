package actors.github

import models.GithubRepository
import play.api.libs.json.JsObject
import spray.client.pipelining._
import spray.http.HttpResponse

import scala.concurrent.Future
import scala.util.{Failure, Success}


class GithubEventsTradeActor extends AbstractGithubActor {

  import system.dispatcher // execution context for futures

  override def receive: Receive = {

    case repo: GithubRepository =>
      getEvents(repo.owner, repo.name) onComplete {
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

  }

  /**
   * List events for a repository
   *
   * GET /repos/:owner/:repo/issues/events
   *
   * @param owner
   * @param repo
   * @return
   */
  private def getEvents(owner: String, repo: String): Future[HttpResponse] = {
    pipeline(
      Get(
        s"${AbstractGithubActor.githubApiUrl}/repos/$owner/$repo/issues/events" +
          s"?client_id=${AbstractGithubActor.client_id}" +
          s"&client_secret=${AbstractGithubActor.client_secret}" +
          s"&per_page=${100}"
      )
    )
  }

}
