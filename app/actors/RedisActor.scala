package actors

import akka.actor.{PoisonPill, Actor}
import com.redis._
import play.api.libs.json.JsValue
import play.api.Logger

case class RedisRepository(name: String, owner: String, issues: List[JsValue])

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
    case repo: RedisRepository => {
      Logger.debug(s"RedisActor | Repo reçu pour sauvegarde : ${repo.owner}/${repo.name}")
      
      RedisActor.clients.withClient {
        client => {
          val key = s"${repo.owner}:${repo.name}"

          client.del(key)

          client.hset(RedisActor.MASTER_KEY, key, repo.issues)

          Logger.debug(s"RedisActor | N° of key inserted in $key : ${client.hgetall(RedisActor.MASTER_KEY).get.size}")
        }
      }
      self ! PoisonPill
    }

    case error: Exception =>
      Logger.error(s"RedisActor | ERROR : ${error.getMessage}")
      throw error
  }

}
