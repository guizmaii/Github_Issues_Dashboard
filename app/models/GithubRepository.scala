package models

import play.api.db.slick.Config.driver.simple._

case class GithubRepository(id: Option[Long],
                owner: String,
                name: String,
                issuesNumber: Int)


class GithubRepositoryTable(tag: Tag) extends Table[GithubRepository](tag, "REPOS") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def owner = column[String]("OWNER", O.NotNull)
  def name = column[String]("NAME", O.NotNull)
  def issuesNumber = column[Int]("ISSUESNUMBER")

  def * = (id.?, owner, name, issuesNumber) <> (GithubRepository.tupled, GithubRepository.unapply)
}

object GithubRepositoryDAO {

  private val repos = TableQuery[GithubRepositoryTable]

  def get(id: Long)(implicit s: Session): GithubRepository = repos.where(_.id === id).first

  def getAll(implicit s: Session): List[GithubRepository] = repos.list()

  def insert(repo: GithubRepository)(implicit s: Session): Int = repos insert repo

  def delete(id: Long)(implicit s: Session): Int = repos.where(_.id === id).delete

  def exists(repo: GithubRepository)(implicit s: Session): Boolean = {
    repos.where(_.name === repo.name).where(_.owner === repo.owner).exists.run
  }

  def notExists(repo: GithubRepository)(implicit s: Session): Boolean = ! exists(repo)

}
