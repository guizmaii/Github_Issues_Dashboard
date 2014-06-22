package actors.compute.G4

import actors.github.{CalculationFinishedEvent, RepositoryData}
import akka.actor.{ActorLogging, Actor}
import domain.{G4Type, GraphType}
import models.GithubRepository
import play.api.libs.json.JsString
import services.RedisClient

case class G4ComputedData(repo: GithubRepository, computedData: Map[String, Int], graphType: GraphType = G4Type)

class G4Actor extends Actor with ActorLogging {

  override def receive: Receive = {

    case data: RepositoryData =>
      var computedData = Map[String, Int]()
      data.issues map {
        issue =>
          val state = (issue \ "state").asInstanceOf[JsString].value
          computedData = computedData + (state -> (computedData.getOrElse(state, 0) + 1))
      }

      RedisClient.getInstance ! G4ComputedData(data.repo, computedData)
      sender ! CalculationFinishedEvent()

  }

}
