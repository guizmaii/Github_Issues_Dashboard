package actors

import actors.compute.G1.G1ComputedData
import actors.compute.G4.G4ComputedData
import akka.actor.{Actor, ActorLogging}
import redis.Redis

// TODO : Passer au driver non bloquant de Redis : https://github.com/debasishg/scala-redis-nb

class RedisActor extends Actor with ActorLogging {

  override def receive: Receive = {

    case g1Data: G1ComputedData =>
      log.debug(s"G1 reçu : ${g1Data.repo}")
      Redis.g1SetAll(g1Data)

    case g4Data: G4ComputedData =>
      log.debug(s"G4 reçu : ${g4Data.repo}")
      Redis.g4SetAll(g4Data)

  }

}
