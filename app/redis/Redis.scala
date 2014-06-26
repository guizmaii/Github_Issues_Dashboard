package redis

import actors.compute.G1.G1ComputedData
import actors.compute.G4.G4ComputedData
import com.redis.{RedisClient, RedisClientPool}
import domain.{G1Type, G4Type}
import models.GithubRepository
import play.api.Play

object Redis {

  private val host: String = Play.current.configuration.getString("redis.host").get
  private val port: Int = Integer.parseInt(Play.current.configuration.getString("redis.port").get)

  val pool = new RedisClientPool(host, port)

  def getG1RedisKey(repoOwner: String, repoName: String): String = {
    s"$repoOwner::$repoName::$G1Type"
  }

  val g4Key: String = G4Type.toString

  import com.redis.serialization.Parse.Implicits._

 // ----- G1 methods

  def g1Get(repo: GithubRepository): Map[Long, Int] =
    pool.withClient {
      _.hgetall[Long, Int](getG1RedisKey(repo.owner, repo.name)).get
    }

  def g1SetAll(g1Data: G1ComputedData): Boolean =
    pool.withClient {
      _.hmset(getG1RedisKey(g1Data.repo.owner, g1Data.repo.name), g1Data.computedData)
    }

  // ----- G4 methods

  def g4GetAll: Map[String, Int] =
    pool.withClient {
      _.hgetall[String, Int](g4Key).get
    }

  def g4SetAll(g4Data: G4ComputedData) =
    pool.withClient { implicit client =>
      g4Data.computedData map { t => g4Set(t._1, t._2) }
    }

  private def g4Set(state: String, occurences: Int)(implicit redis: RedisClient) =
    redis.hget(g4Key, state) match {
      case None => redis.hset(g4Key, state, occurences)
      case value: Some[String] => redis.hset(g4Key, state, value.get.toInt + occurences)
    }

}
