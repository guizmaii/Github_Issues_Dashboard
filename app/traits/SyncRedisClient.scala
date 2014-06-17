package traits

import com.redis.RedisClientPool
import domain.GraphType

trait SyncRedisClient {

  import play.api.Play

  val host: String = Play.current.configuration.getString("redis.host").get
  val port: Int = Integer.parseInt(Play.current.configuration.getString("redis.port").get)

  val redisPool = new RedisClientPool(host, port)

  def getRedisKey(repoOwner: String, repoName: String, graphType: GraphType): String = {
    s"$repoOwner::$repoName::$graphType"
  }

}
