package actors.compute.G1

import actors.github.RepositoryData
import akka.actor._
import domain.{G1Type, GraphType}
import models.GithubRepository
import play.api.libs.json._
import services.RedisClient

import scala.collection.mutable

case class G1ComputedData(repo: GithubRepository, computedData: mutable.Map[Long, Int], graphType: GraphType = G1Type)

private case class G1Data(issuesChunk: List[JsObject], issues: List[JsObject])

case class CalculationFinishedEvent()

object G1Actor {
  val availableProcessor: Int = Runtime.getRuntime.availableProcessors
}

class G1Actor extends Actor with ActorLogging {

  val graphPoints = mutable.Map[Long, Int]()
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

      val groupedIssuesIterator = data.issues.grouped( optimisedChunkSize(data.issues.size) ).toList
      workers = groupedIssuesIterator.length
      groupedIssuesIterator map (
        list =>
          context.actorOf(
            Props[G1Calculator],
            s"G1Calculator_${groupedIssuesIterator.indexOf(list)}"
          ) ! G1Data(list,  data.issues)
      )

    case calculatedGraphPoints: mutable.Map[Long, Int] =>
      this.graphPoints ++= calculatedGraphPoints
      workers -= 1
      if (workers == 0) {
        end = System.currentTimeMillis()
        log.debug("Temps de calcul : " + ((end - begin) / 1000) + " secondes")

        RedisClient.getInstance ! G1ComputedData(repo, graphPoints)
        githhubActor ! CalculationFinishedEvent()
      }

  }

  private def optimisedChunkSize(listSize: Int): Int = {
    // La valeur 1000 ici est arbitraire, issue de l'expérience.
    listSize < 1000 match {
      case true => listSize
      case false => listSize / (2 * G1Actor.availableProcessor)
    }
  }

}
