package actors.compute.G1

import akka.actor.Actor
import org.joda.time.DateTime
import play.api.libs.json.{JsNull, JsObject, JsString}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class G1Calculator extends Actor {

  val calculatedGraphPoints = mutable.Map[Long, Int]()

  override def receive: Receive = {

    case data: G1Data =>
      val lighterList = getLighterList(data.issuesChunk)
      lighterList map {
        tuple =>
          val parsedCreatedDate = DateTime.parse(tuple._1)
          calculatedGraphPoints += parsedCreatedDate.toDate.getTime -> lighterList.count(isOpenAtThisDate(_, parsedCreatedDate))
      }
      sender ! calculatedGraphPoints
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

  private def isOpenAtThisDate(tuple: (String, String), creationDate: DateTime): Boolean = {
    isCreatedBeforeOrInSameTime(tuple._1, creationDate) && isClosedAfter(tuple._2, creationDate)
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

}
