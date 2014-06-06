package actors.compute

import akka.actor.{PoisonPill, Actor}
import actors.{RepositoryData, GithubRepository, Redisable}
import play.api.Logger
import org.joda.time.DateTime
import scala.collection.mutable
import domain.{G1, GraphType}
import play.api.libs.json.JsValue

case class G1ComputedData(repo: GithubRepository, computedData: mutable.Map[String, Int], graphType: GraphType = G1)

class G1Actor extends Actor with Redisable {

  val graphPoints = mutable.Map[String, Int]()

  override def receive: Receive = {

    case data: RepositoryData =>
      data.issues map {
        issue =>
          val createdDate = (issue \ "created_at").as[String]
          val parsedCreatedDate = DateTime.parse(createdDate)
          graphPoints(createdDate) = data.issues.count(isOpenAtThisDate(_, parsedCreatedDate))
      }
      redisActor ! G1ComputedData(data.repo, graphPoints)
      self ! PoisonPill

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      // TODO : Valider l'utiliter de s'envoyer une PoisonPill
      self ! PoisonPill
      throw error
  }

  // TODO : Validate
  private def isCreatedBefore(issue: JsValue, creationDate: DateTime): Boolean = {
    DateTime.parse((issue \ "created_at").as[String]).isBefore(creationDate)
  }

  // TODO : Validate
  private def isClosedAfter(issue: JsValue, creationDate: DateTime): Boolean = {
    (issue \ "closed_at").asOpt[String] match {
      case closedDate: Some[String] =>
        DateTime.parse(closedDate.get).isAfter(creationDate)
      case None =>
        // Si l'issue n'est pas closed alors
        // elle sera forcément fermé après la "creationDate"
        true
    }
  }

  // TODO : Validate
  private def isOpenAtThisDate(issue: JsValue, creationDate: DateTime): Boolean = {
    isCreatedBefore(issue, creationDate) && isClosedAfter(issue, creationDate)
  }

}
