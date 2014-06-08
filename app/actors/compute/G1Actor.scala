package actors.compute

import akka.actor.{Props, PoisonPill, Actor}
import actors.{GithubRepository, Redisable}
import play.api.Logger
import org.joda.time.DateTime
import domain.{G1, GraphType}

import play.api.libs.json._
import scala.collection.mutable.ListBuffer
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
      groupedIssuesIterator map ( Akka.system.actorOf(Props[G1Actor]) ! G1Data(_,  data.issues) )

    case data: G1Data =>
      val lighterList = getLighterList(data.issuesChunk)
      lighterList map {
        tuple =>
          val parsedCreatedDate = DateTime.parse(tuple._1)
          graphPoints.put(tuple._1, lighterList.count(isOpenAtThisDate(_, parsedCreatedDate)))
      }
      sender ! graphPoints

    case graphPointsChunk: java.util.TreeMap[String, Int] =>
      this.graphPoints.putAll(graphPointsChunk)
      workers -= 1
      if (workers == 0) {
        end = System.currentTimeMillis()
        Logger.debug("TEMPS PRIS : " + ((end - begin) / 1000) + " secondes")

        redisActor ! G1ComputedData(repo, graphPoints)
      }

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      // TODO : Valider l'utiliter de s'envoyer une PoisonPill
      self ! PoisonPill
      throw error
  }

  private def getLighterList(issues: List[JsObject]): ListBuffer[(String, String)] = {
    val lightIssueList = new ListBuffer[(String, String)]()
    issues map {
      issue =>
        val created_at = (issue \ "created_at").asInstanceOf[JsString].value
        val closed_at = issue \ "closed_at" match {
          case json: JsString => json.value
          case JsNull => null
        }
        lightIssueList += (created_at -> closed_at)
    }
    lightIssueList
  }

  private def isCreatedBeforeOrInSameTime(created_at: String, creationDate: DateTime): Boolean = {
    val createdAt = DateTime.parse(created_at)
    createdAt.isBefore(creationDate) || createdAt.isEqual(creationDate)
  }

  private def isClosedAfter(closed_at: String, creationDate: DateTime): Boolean = {
    closed_at match {
      case value: String =>
        DateTime.parse(value).isAfter(creationDate)
      case null =>
        // Si l'issue n'est pas closed alors
        // elle sera forcément fermé après la "creationDate"
        true
    }
  }

  private def isOpenAtThisDate(tuple: (String, String), creationDate: DateTime): Boolean = {
    isCreatedBeforeOrInSameTime(tuple._1, creationDate) && isClosedAfter(tuple._2, creationDate)
  }

}
