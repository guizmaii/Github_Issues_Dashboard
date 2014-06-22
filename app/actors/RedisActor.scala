package actors

import actors.compute.G1.G1ComputedData
import actors.compute.G4.G4ComputedData
import akka.actor.{Actor, ActorLogging}
import domain.G4Type
import traits.SyncRedisClient

class RedisActor extends Actor with ActorLogging with SyncRedisClient {

  override def receive: Receive = {

    case g1Data: G1ComputedData =>
      val key = getG1RedisKey(g1Data.repo.owner, g1Data.repo.name, g1Data.graphType)

      log.debug(s"Repo reçu pour sauvegarde : $key")

      redisPool.withClient {
        client => {
          client.hmset(key, g1Data.computedData)
        }
      }

    case g4Data: G4ComputedData =>
      log.debug(s"G4 reçu : ${g4Data.repo}")
      redisPool.withClient { client =>
        g4Data.computedData map {
          t =>
            client.hget(G4key, t._1) match {
              case None => client.hset(G4key, t._1, t._2)
              case value: Some[String] => client.hset(G4key, t._1, value.get.toInt + t._2)
            }
        }
      }

  }

}
