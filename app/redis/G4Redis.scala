package redis

import actors.compute.G4.G4ComputedData
import domain.G4Type
import models.GithubRepository

object G4Redis extends Redis {

  override def key(repo: GithubRepository): String = abstractKey(repo, G4Type)

  import com.redis.serialization.Parse.Implicits._

  /**
   * Renvoie :
   *
   * {
   *  state1: nbOccurences,
   *  state2: nbOccurences,
   *  ...
   *  statex: nbOccurences,
   * }
   *
   * @param repo
   * @return
   */
  def get(repo: GithubRepository): Map[String, Int] =
    Redis.pool.withClient {
      _.hgetall[String, Int](key(repo)).get
    }

  def getAll(repos: List[GithubRepository]): List[Map[String, Int]] = repos map get

  def setAll(g4Data: G4ComputedData) =
    Redis.pool.withClient {
      _.hmset(key(g4Data.repo), g4Data.computedData)
    }

}
