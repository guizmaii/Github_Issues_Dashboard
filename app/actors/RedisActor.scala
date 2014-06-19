package actors

import actors.compute.G1.G1ComputedData
import akka.actor.{Actor, ActorLogging}
import traits.SyncRedisClient

class RedisActor extends Actor with ActorLogging with SyncRedisClient {

  override def receive: Receive = {

    case g1Data: G1ComputedData =>
      val key = getRedisKey(g1Data.repo.owner, g1Data.repo.name, g1Data.graphType)

      log.debug(s"Repo reçu pour sauvegarde : $key")

      redisPool.withClient {
        client => {
          client.hmset(key, g1Data.computedData)
        }
      }

  }

}
