package actors

import akka.actor.Actor
import com.redis._
import play.api.Logger
import actors.compute.G1ComputedData

object RedisActor {

  import play.api.Play

  val host: String = Play.current.configuration.getString("redis.host").get
  val port: Int = Integer.parseInt(Play.current.configuration.getString("redis.port").get)

  val clients = new RedisClientPool(host, port)

  val MASTER_KEY = "data"

}

class RedisActor extends Actor {

  override def receive: Receive = {

    // TODO : Coder & Tester la récupération des données
    case g1Data: G1ComputedData =>
      val key = s"${g1Data.repo.owner}:${g1Data.repo.name}:${g1Data.graphType}"

      Logger.debug(s"${this.getClass} | Repo reçu pour sauvegarde : $key")

      RedisActor.clients.withClient {
        client => {
          client.del(key)
          client.hset(RedisActor.MASTER_KEY, key, g1Data.computedData)

          // TODO : To delete
          Logger.debug(s"${this.getClass} | N° of key inserted in $key : ${client.hgetall(RedisActor.MASTER_KEY).get.size}")
        }
      }

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
  }

}
