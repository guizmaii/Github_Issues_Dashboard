package actors.compute.G1

import akka.actor.Actor
import org.joda.time.DateTime

class G1Calculator extends Actor {

  override def receive: Receive = {

    case g1Data: G1Data =>
      sender ! (g1Data.periodChunk map {
        date =>
          jodaDateTimeToTimestamp(date) -> g1Data.lightIssues.count(isOpenAtThisDate(_, date))
      }).toMap[Long, Int]

  }

  private def jodaDateTimeToTimestamp(date: DateTime) = date.toDate.getTime

  private def isOpenAtThisDate(issue: LightIssue, date: DateTime): Boolean = {
    isCreatedBeforeOrAtTheSameDate(issue.created_at, date) && isClosedAfter(issue.closed_at, date)
  }

  private def isCreatedBeforeOrAtTheSameDate(created_at: DateTime, date: DateTime): Boolean = {
    created_at.isBefore(date) || created_at.isEqual(date)
  }

  private def isClosedAfter(closed_at: DateTime, date: DateTime): Boolean = {
    closed_at match {
      case closed_at: DateTime =>
        closed_at.isAfter(date)
      case null =>
        // Si l'issue n'est pas closed alors
        // elle sera forcément fermé après la "creationDate"
        true
    }
  }

}
