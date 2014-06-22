package actors.compute.G1

import actors.github.{CalculationFinishedEvent, RepositoryData}
import akka.actor._
import domain.{G1Type, GraphType}
import models.GithubRepository
import org.joda.time.{DateTime, Days, DurationFieldType}
import play.api.libs.json._
import services.RedisClient

case class G1ComputedData(repo: GithubRepository, computedData: Map[Long, Int], graphType: GraphType = G1Type)

private case class LightIssue(created_at: DateTime, closed_at: DateTime)

private case class G1Data(periodChunk: List[DateTime], lightIssues: List[LightIssue])

object G1Actor {

  val availableProcessors: Int = Runtime.getRuntime.availableProcessors

  // Date de lancement de Github (voir Wikipedia) : 01/04/2008
  // Ce sera notre année 0 en qq sorte ou encore, pour les informaticiens, cela équivaut au 01/01/1970 du temps Posix.
  val githubOpenDate = new DateTime(2008, 4, 1, 0, 0)
}

class G1Actor extends Actor with ActorLogging {

  var graphPoints = Map[Long, Int]()
  var repo: GithubRepository = null

  var begin = 0L
  var end = 0L

  var workers = 0

  var githhubActor: ActorRef = null

  private val daysBetweenGithubOpenDateAndToday: List[DateTime] = {
    val days: Int = Days.daysBetween(G1Actor.githubOpenDate, new DateTime()).getDays
    (for (i <- 0 to days)
      yield G1Actor.githubOpenDate.withFieldAdded(DurationFieldType.days, i)).toList
  }

  override def receive: Receive = {

    case data: RepositoryData =>
      begin = System.currentTimeMillis()

      githhubActor = sender()
      repo = data.repo

      val blocks = daysBetweenGithubOpenDateAndToday.grouped( optimisedChunkSize(daysBetweenGithubOpenDateAndToday.size) ).toList
      workers = blocks.length
      val lighterList = getLighterList(data.issues)
      blocks map (
        periodChunk =>
          context.actorOf(
            Props[G1Calculator],
            s"G1Calculator_${blocks.indexOf(periodChunk)}"
          ) ! G1Data(periodChunk,  lighterList)
      )

    case computedGraphPoints: Map[Long, Int] =>
      this.graphPoints = this.graphPoints ++ computedGraphPoints
      workers -= 1
      if (allWorkersHasFinished) {
        end = System.currentTimeMillis()
        log.debug("Temps de calcul : " + ((end - begin) / 1000) + " secondes")

        RedisClient.getInstance ! G1ComputedData(repo, graphPoints)
        githhubActor ! CalculationFinishedEvent()
      }

  }

  def allWorkersHasFinished: Boolean = {
    workers == 0
  }

  private def optimisedChunkSize(listSize: Int): Int = {
    listSize / G1Actor.availableProcessors
  }

  private def getLighterList(issues: List[JsObject]): List[LightIssue] = {
    (issues map {
      issue =>
        val created_at = new DateTime((issue \ "created_at").asInstanceOf[JsString].value)
        val closed_at = issue \ "closed_at" match {
          case json: JsString => new DateTime(json.value)
          case JsNull => null
        }
        LightIssue(created_at, closed_at)
    }).toList
  }

}
