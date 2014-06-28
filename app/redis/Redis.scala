package redis

import com.redis.RedisClientPool
import domain.GraphType
import models.GithubRepository
import play.api.Play

private[redis] object Redis {

  private val host: String = Play.current.configuration.getString("redis.host").get
  private val port: Int = Integer.parseInt(Play.current.configuration.getString("redis.port").get)

  val pool = new RedisClientPool(host, port)

}

abstract class Redis {

  protected def abstractKey(repo: GithubRepository, graphType: GraphType): String =
    s"${repo.owner}::${repo.name}::$graphType"

  def key(repo: GithubRepository): String

  def delete(repo: GithubRepository) =
    Redis.pool.withClient {
      _.del(key(repo))
    }

}
