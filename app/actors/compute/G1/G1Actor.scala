package actors.compute.G1

import akka.actor.{Props, Actor}
import actors.{GithubRepository, Redisable}
import play.api.Logger
import domain.{G1, GraphType}

import play.api.libs.json._
import actors.RepositoryData
import play.api.libs.concurrent.Akka

case class G1ComputedData(repo: GithubRepository, computedData: java.util.TreeMap[String, Int], graphType: GraphType = G1)

private case class G1Data(issuesChunk: List[JsObject], issues: List[JsObject])

object G1Actor {
  val CHUNK_SIZE = 1000
}

class G1Actor extends Actor with Redisable {

  import play.api.Play.current

  val graphPoints = new java.util.TreeMap[String, Int]()
  var repo: GithubRepository = null

  var begin = 0L
  var end = 0L

  var workers = 0

  override def receive: Receive = {

    case data: RepositoryData =>
      begin = System.currentTimeMillis()

      repo = data.repo

      val groupedIssuesIterator = data.issues.grouped(G1Actor.CHUNK_SIZE).toList
      workers = groupedIssuesIterator.length
      groupedIssuesIterator map ( Akka.system.actorOf(Props[G1Calculator]) ! G1Data(_,  data.issues) )

    case graphPointsChunk: java.util.TreeMap[String, Int] =>
      this.graphPoints.putAll(graphPointsChunk)
      workers -= 1
      if (workers == 0) {
        end = System.currentTimeMillis()
        Logger.debug("TEMPS PRIS : " + ((end - begin) / 1000) + " secondes")

        redisActor ! G1ComputedData(repo, graphPoints)
      }

  }

}
