package models

import play.api.db.slick.Config.driver.simple._

case class GithubRepository(id: Option[Long], owner: String, name: String, isFetchedInRedis: Boolean = false)

class GithubRepositoryTable(tag: Tag) extends Table[GithubRepository](tag, "REPOS") {

  def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def owner = column[String]("OWNER", O.NotNull)
  def name = column[String]("NAME", O.NotNull)
  def isFetchedInRedis = column[Boolean]("IS_FETCHED_IN_REDIS", O.NotNull)

  def * = (id.?, owner, name, isFetchedInRedis) <> (GithubRepository.tupled, GithubRepository.unapply)
}

object GithubRepositoryDAO {

  private val repos = TableQuery[GithubRepositoryTable]

  def get(id: Long)(implicit s: Session): GithubRepository = repos.where(_.id === id).first

  def getAll(implicit s: Session): List[GithubRepository] = repos.list()

  def getAllFetched(implicit s: Session): List[GithubRepository] = repos.where(_.isFetchedInRedis === true).list()

  def getAllNonAlreadyFetched(implicit s: Session): List[GithubRepository] =
    repos.where(_.isFetchedInRedis === false).list()

  def markAsFetched(repo: GithubRepository)(implicit s: Session) = {
    repos.where(_.name === repo.name).where(_.owner === repo.owner).map(_.isFetchedInRedis).update(true)
  }

  def insert(repo: GithubRepository)(implicit s: Session): Int = repos insert repo

  def delete(id: Long)(implicit s: Session): Int = repos.where(_.id === id).delete

  def exists(repo: GithubRepository)(implicit s: Session): Boolean =
    repos.where(_.name === repo.name).where(_.owner === repo.owner).exists.run

  def notExists(repo: GithubRepository)(implicit s: Session): Boolean = ! exists(repo)

}
