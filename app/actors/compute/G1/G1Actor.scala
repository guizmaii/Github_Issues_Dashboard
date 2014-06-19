package actors.compute.G1

import actors.RepositoryData
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
  val CHUNK_SIZE = 1000
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

      val groupedIssuesIterator = data.issues.grouped(G1Actor.CHUNK_SIZE).toList
      workers = groupedIssuesIterator.length
      groupedIssuesIterator map (
        list =>
          context.actorOf(
            Props[G1Calculator],
            s"${repo.owner}_${repo.name}_${groupedIssuesIterator.indexOf(list)}"
          ) ! G1Data(list,  data.issues)
      )

    case calculatedGraphPoints: mutable.Map[Long, Int] =>
      this.graphPoints ++= calculatedGraphPoints
      workers -= 1
      if (workers == 0) {
        end = System.currentTimeMillis()
        log.debug("TEMPS PRIS : " + ((end - begin) / 1000) + " secondes")

        RedisClient.getInstance ! G1ComputedData(repo, graphPoints)
        githhubActor ! CalculationFinishedEvent()
        context.stop(self)
      }

  }

}
