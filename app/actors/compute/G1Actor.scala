package actors.compute

import akka.actor.{PoisonPill, Actor}
import actors.{RepositoryData, GithubRepository, Redisable}
import play.api.Logger
import org.joda.time.DateTime
import domain.{G1, GraphType}

import play.api.libs.json._

import play.api.libs.json.Reads._ // Custom validation helpers

case class G1ComputedData(repo: GithubRepository, computedData: java.util.TreeMap[String, Int], graphType: GraphType = G1)

class G1Actor extends Actor with Redisable {

  val graphPoints = new java.util.TreeMap[String, Int]()

  override def receive: Receive = {

    case data: RepositoryData =>
      val before = System.currentTimeMillis()
      data.issues map {
        issue =>
          val createdDate = (issue \ "created_at").as[String]
          val parsedCreatedDate = DateTime.parse(createdDate)
          graphPoints.put(createdDate, data.issues.count(isOpenAtThisDate(_, parsedCreatedDate)))
      }
      val after = System.currentTimeMillis()

      Logger.debug("time : " + ((after - before) / 1000) + " secondes")

      redisActor ! G1ComputedData(data.repo, graphPoints)
      self ! PoisonPill

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      // TODO : Valider l'utiliter de s'envoyer une PoisonPill
      self ! PoisonPill
      throw error
  }

  private def isCreatedBeforeOrInSameTime(issue: JsObject, creationDate: DateTime): Boolean = {
    val createdAt = DateTime.parse((issue \ "created_at").asInstanceOf[JsString].value)
    createdAt.isBefore(creationDate) || createdAt.isEqual(creationDate)
  }

  private def isClosedAfter(issue: JsObject, creationDate: DateTime): Boolean = {
    issue \ "closed_at" match {
      case closedDate: JsString =>
        DateTime.parse(closedDate.value).isAfter(creationDate)
      case JsNull =>
        // Si l'issue n'est pas closed alors
        // elle sera forcément fermé après la "creationDate"
        true
      case otherType =>
        Logger.error(s"${this.getClass} | ERROR : 'closed_at' received : $otherType")
        false
    }
  }

  // TODO : Validate
  private def isOpenAtThisDate(issue: JsObject, creationDate: DateTime): Boolean = {
    isCreatedBeforeOrInSameTime(issue, creationDate) && isClosedAfter(issue, creationDate)
  }

}
