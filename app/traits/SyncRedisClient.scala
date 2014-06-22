package traits

import com.redis.RedisClientPool
import domain.{G4Type, GraphType}

trait SyncRedisClient {

  import play.api.Play

  val host: String = Play.current.configuration.getString("redis.host").get
  val port: Int = Integer.parseInt(Play.current.configuration.getString("redis.port").get)

  val redisPool = new RedisClientPool(host, port)

  def getG1RedisKey(repoOwner: String, repoName: String, graphType: GraphType): String = {
    s"$repoOwner::$repoName::$graphType"
  }

  val G4key: String = G4Type.toString

}
