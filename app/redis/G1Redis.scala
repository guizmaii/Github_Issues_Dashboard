package redis

import actors.compute.G1.G1ComputedData
import domain.G1Type
import models.GithubRepository

import scala.collection.immutable.TreeMap

object G1Redis extends Redis {

  override def key(repo: GithubRepository): String = abstractKey(repo, G1Type)

  import com.redis.serialization.Parse.Implicits._

  /**
   * Renvoie :
   *
   * {
   *  timestamp: nbIssues,
   *  timestamp2: nbIssues,
   *  ...
   *  timestampx: nbIssues,
   * }
   *
   * @param repo
   * @return
   */
  def get(repo: GithubRepository): TreeMap[Long, Int] =
    TreeMap[Long, Int]() ++ Redis.pool.withClient {
      _.hgetall[Long, Int](key(repo)).get
    }

  def getAll(repos: List[GithubRepository]): List[TreeMap[Long, Int]] = repos map get

  def setAll(g1Data: G1ComputedData): Boolean =
    Redis.pool.withClient {
      _.hmset(key(g1Data.repo), g1Data.computedData)
    }

}
