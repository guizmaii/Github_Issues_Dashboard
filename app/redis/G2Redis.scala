package redis

import actors.compute.G2.G2ComputedData
import domain.G2Type
import models.GithubRepository

object G2Redis extends Redis {

  override def key(repo: GithubRepository): String = abstractKey(repo, G2Type)

  import com.redis.serialization.Parse.Implicits._

  def set(data: G2ComputedData): Boolean =
    Redis.pool.withClient {
      _.set(key(data.repo), data.avgTimeToCloseAnIssueInSeconds)
    }

  def get(repo: GithubRepository): Long =
    Redis.pool.withClient {
      _.get[Long](key(repo)).get
    }

}
