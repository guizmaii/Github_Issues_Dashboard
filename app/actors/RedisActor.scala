package actors

import akka.actor.Actor
import com.redis._
import play.api.libs.json.JsValue
import play.api.Logger

case class RedisRepository(name: String, owner: String, issues: List[JsValue])

object RedisActor {

  import play.api.Play

  val host: String = Play.current.configuration.getString("redis.host").get
  val port: Int = Integer.parseInt(Play.current.configuration.getString("redis.port").get)

  val clients = new RedisClientPool(host, port)

}

class RedisActor extends Actor {

  override def receive: Receive = {

    // TODO : Coder & Tester la récupération des données
    case repo: RedisRepository =>
      Logger.debug(s"RedisActor | Repo reçu pour sauvegarde : ${repo.owner}/${repo.name}")
      
      RedisActor.clients.withClient {
        client => {
          val key = s"data:${repo.owner}:${repo.name}"

          client.del(key)

          repo.issues.map(client.rpush(key, _))
          Logger.info(s"RedisActor | N° of key inserted in key ($key) : ${client.llen(key)}")
        }
      }

    case error: Exception =>
      Logger.error(s"RedisActor | ERROR : ${error.getMessage}")
  }

}
