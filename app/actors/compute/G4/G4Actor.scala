package actors.compute.G4

import actors.github.{CalculationFinishedEvent, RepositoryData}
import akka.actor.{Actor, ActorLogging}
import models.GithubRepository
import play.api.libs.json.JsString
import redis.RedisActorSingleton

case class G4ComputedData(repo: GithubRepository, computedData: Map[String, Int])

class G4Actor extends Actor with ActorLogging {

  override def receive: Receive = {

    case data: RepositoryData =>
      var computedData = Map[String, Int]()
      data.issues map {
        issue =>
          val state = (issue \ "state").asInstanceOf[JsString].value
          computedData = computedData + (state -> (computedData.getOrElse(state, 0) + 1))
      }

      RedisActorSingleton.instance ! G4ComputedData(data.repo, computedData)
      sender ! CalculationFinishedEvent()

  }

}
