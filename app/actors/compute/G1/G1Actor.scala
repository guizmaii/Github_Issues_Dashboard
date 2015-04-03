package actors.compute.G1

import actors.github.{CalculationFinishedEvent, RepositoryData}
import akka.actor._
import helpers.TimeHelper
import models.GithubRepository
import org.joda.time.{DateTime, Days, DurationFieldType}
import play.api.libs.json._
import redis.RedisActorSingleton

case class G1ComputedData(repo: GithubRepository, computedData: Map[Long, Int])
private case class LightIssue(created_at: DateTime, closed_at: DateTime)
private case class G1Data(periodChunk: List[DateTime], lightIssues: List[LightIssue])

class G1Actor extends Actor with ActorLogging {

  private val daysBetweenGithubOpenDateAndToday: Seq[DateTime] = {
    for (i <- 0 to Days.daysBetween(TimeHelper.githubOpenDate, new DateTime()).getDays)
      yield TimeHelper.githubOpenDate.withFieldAdded(DurationFieldType.days, i)
  }

  var graphPoints = Map[Long, Int]()
  var repo: GithubRepository = null
  var begin = 0L
  var end = 0L
  var workers = 0
  var githhubActor: ActorRef = null

  override def receive: Receive = {

    case data: RepositoryData =>
      begin = System.currentTimeMillis()

      githhubActor = sender()
      repo = data.repo

      val blocks = daysBetweenGithubOpenDateAndToday.grouped( optimisedChunkSize(daysBetweenGithubOpenDateAndToday.size) ).toList
      workers = blocks.length
      val lighterList = getLighterList(data.issues)
      blocks foreach (periodChunk =>
        context.actorOf(Props[G1Calculator], s"G1Calculator_${blocks.indexOf(periodChunk)}") ! G1Data(periodChunk, lighterList)
      )

    case computedGraphPoints: Map[Long, Int] =>
      this.graphPoints = this.graphPoints ++ computedGraphPoints
      workers -= 1
      if (allWorkersHasFinished) {
        end = System.currentTimeMillis()
        log.debug("Temps de calcul : " + ((end - begin) / 1000) + " secondes")

        RedisActorSingleton.instance ! G1ComputedData(repo, graphPoints)
        githhubActor ! CalculationFinishedEvent()
      }

  }

  def allWorkersHasFinished: Boolean = {
    workers == 0
  }

  private def optimisedChunkSize(listSize: Int): Int = {
    listSize / Runtime.getRuntime.availableProcessors
  }

  private def getLighterList(issues: List[JsObject]): List[LightIssue] = {
    issues map { issue =>
      val created_at = new DateTime((issue \ "created_at").asInstanceOf[JsString].value)
      val closed_at = issue \ "closed_at" match {
        case json: JsString => new DateTime(json.value)
        case JsNull => null
      }
      LightIssue(created_at, closed_at)
    }
  }

}
