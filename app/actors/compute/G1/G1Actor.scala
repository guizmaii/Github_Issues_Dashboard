package actors.compute.G1

import akka.actor.{Props, Actor}
import actors.GithubRepository
import play.api.Logger
import domain.{G1Type, GraphType}

import play.api.libs.json._
import actors.RepositoryData
import play.api.libs.concurrent.Akka
import traits.AsyncRedisable
import scala.collection.mutable

case class G1ComputedData(repo: GithubRepository, computedData: mutable.Map[Long, Int], graphType: GraphType = G1Type)

private case class G1Data(issuesChunk: List[JsObject], issues: List[JsObject])

object G1Actor {
  val CHUNK_SIZE = 1000
}

class G1Actor extends Actor with AsyncRedisable {

  import play.api.Play.current

  val graphPoints = mutable.Map[Long, Int]()
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

    case calculatedGraphPoints: mutable.Map[Long, Int] =>
      this.graphPoints ++= calculatedGraphPoints
      workers -= 1
      if (workers == 0) {
        end = System.currentTimeMillis()
        Logger.debug("TEMPS PRIS : " + ((end - begin) / 1000) + " secondes")

        redisActor ! G1ComputedData(repo, graphPoints)
      }

  }

}
