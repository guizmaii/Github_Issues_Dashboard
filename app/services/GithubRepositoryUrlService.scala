package services

import models.DBRepo

object GithubRepositoryUrlService {

  private val githubUrl = "https://github.com"

  lazy val regexValidator = s"""$githubUrl/[a-zA-Z0-9]+/[a-zA-Z0-9]+""".r
  
  def parseUrl(url: String): DBRepo = {
    val repoInfos = url.split(s"$githubUrl/")(1).split("/")
    // TODO : Aller chercher le nb d'issues du dépot
    DBRepo(None, repoInfos(0), repoInfos(1), 0)
  }

  def getUrl(repo: DBRepo): String = {
    s"$githubUrl/${repo.owner}/${repo.name}"
  }

}
