package actors

import actors.compute.G1.G1ComputedData
import akka.actor.Actor
import play.api.Logger
import traits.SyncRedisable

class RedisActor extends Actor with SyncRedisable {

  override def receive: Receive = {

    case g1Data: G1ComputedData =>
      val key = getRedisKey(g1Data.repo.owner, g1Data.repo.name, g1Data.graphType)

      Logger.debug(s"${this.getClass} | Repo reçu pour sauvegarde : $key")

      redisPool.withClient {
        client => {
          client.hmset(key, g1Data.computedData)
        }
      }
  }

}
