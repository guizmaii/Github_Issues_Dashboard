package actors

import actors.compute.G1.G1ComputedData
import actors.compute.G4.G4ComputedData
import akka.actor.{Actor, ActorLogging}
import helpers.TimeHelper
import models.{GithubRepository, GithubRepositoryDAO}
import org.joda.time.DateTime
import play.api.db.slick._
import redis.{G1Redis, G4Redis}

import scala.collection.immutable.TreeMap

// TODO : Passer au driver non bloquant de Redis : https://github.com/debasishg/scala-redis-nb

case class DeleteOrder(repo: GithubRepository)

class RedisActor extends Actor with ActorLogging {

  import play.api.Play.current

  override def receive: Receive = {

    case g1Data: G1ComputedData =>
      log.debug(s"G1 reçu : ${g1Data.repo}")
      G1Redis setAll g1Data
      DB.withTransaction( implicit t => GithubRepositoryDAO.markAsFetched(g1Data.repo) )
      G1Redis setOldestIssueCreationDate oldestIssueCreationDateMinusTwoDays

    case g4Data: G4ComputedData =>
      log.debug(s"G4 reçu : ${g4Data.repo}")
      G4Redis setAll g4Data

    case order: DeleteOrder =>
      log.debug(s"Delete order reçu : ${order.repo}")
      G1Redis delete order.repo
      G4Redis delete order.repo
      G1Redis setOldestIssueCreationDate oldestIssueCreationDateMinusTwoDays
  }

  import play.api.Play.current


  def oldestIssueCreationDateMinusTwoDays: Long =
    TimeHelper.dateTimeToTimestamp(new DateTime(findOldestIssueCreationDate).minusDays(2))

  /**
   * Trouve la date de l'issue la plus ancienne stockée dans Redis
   *
   * @return
   */
  def findOldestIssueCreationDate: Long = {
    var oldestIssueCreationDate = Long.MaxValue
    DB.withTransaction (
      implicit t =>
        G1Redis getAll GithubRepositoryDAO.getAllFetched map {
          data =>
           val oldestIssue = findOldestIssue(data)
           if (oldestIssue._1 < oldestIssueCreationDate) {
             oldestIssueCreationDate = oldestIssue._1
           }
        }
    )
    oldestIssueCreationDate
  }

  def findOldestIssue(map: TreeMap[Long, Int]): (Long, Int) = {
    for(
      t <- map
      if t._2 != 0
    ) return t
    throw new RuntimeException("Ce cas ne peut normalement jamais arriver")
  }

}
