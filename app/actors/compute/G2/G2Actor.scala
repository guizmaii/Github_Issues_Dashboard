package actors.compute.G2

import actors.github.{CalculationFinishedEvent, RepositoryData}
import akka.actor.Actor
import models.GithubRepository
import org.joda.time.{DateTime, Seconds}
import play.api.libs.json.{JsNull, JsString}
import redis.RedisActorSingleton

case class G2ComputedData(repo: GithubRepository, avgTimeToCloseAnIssueInSeconds: Long)

class G2Actor extends Actor {

  override def receive: Receive = {

    case data: RepositoryData =>
      val avgValue = avg(
        data.issues.map {
          issue =>
            val created_at = new DateTime((issue \ "created_at").asInstanceOf[JsString].value)
            val closed_at = issue \ "closed_at" match {
              case closed_at: JsString => new DateTime(closed_at.value)
              case JsNull => null
            }
            created_at -> closed_at
        } filter( _._2 != null ) map {
          tuple =>
            Seconds.secondsBetween(tuple._1, tuple._2).getSeconds.asInstanceOf[Long]
        }
      )
      RedisActorSingleton.instance ! G2ComputedData(data.repo, avgValue)
      sender ! CalculationFinishedEvent()

  }

  private def avg(seconds: List[Long]): Long = seconds.sum / seconds.size

}
