package traits

import com.redis.RedisClientPool

trait SyncRedisable {

  import play.api.Play

  val host: String = Play.current.configuration.getString("redis.host").get
  val port: Int = Integer.parseInt(Play.current.configuration.getString("redis.port").get)

  val clients = new RedisClientPool(host, port)

  val MASTER_KEY = "data"

}
