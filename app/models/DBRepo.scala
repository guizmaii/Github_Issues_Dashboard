package models

import actors.GithubRepository
import play.api.db.slick.Config.driver.simple._

case class DBRepo(id: Option[Long],
                owner: String,
                name: String,
                issuesNumber: Int)


class DBReposTable(tag: Tag) extends Table[DBRepo](tag, "GITHUB_REPOS") {

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def owner = column[String]("owner", O.NotNull)
  def name = column[String]("name", O.NotNull)
  def issuesNumber = column[Int]("issuesNumber")

  def * = (id.?, owner, name, issuesNumber) <> (DBRepo.tupled, DBRepo.unapply)
}

object RepoDAO {

  private val repos = TableQuery[DBReposTable]

  def getAll(implicit s: Session): List[DBRepo] = {
    repos.list()
  }

  def save(repo: DBRepo)(implicit s: Session): Int = {
    repos insert repo
  }

  def delete(id: Long)(implicit s: Session) = {
    repos.where(_.id === id).delete
  }

  def exists(repo: DBRepo)(implicit s: Session): Boolean = {
    repos.where(_.name === repo.name).where(_.owner === repo.owner).exists.run
  }

  def notExists(repo: DBRepo)(implicit s: Session): Boolean = {
    !  exists(repo)
  }

}

