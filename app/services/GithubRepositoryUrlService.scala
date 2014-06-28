package services

import models.GithubRepository

object GithubRepositoryUrlService {

  private val githubUrl = "https://github.com"

  lazy val regexValidator = s"""$githubUrl/[a-zA-Z0-9_-]+/[a-zA-Z0-9_-]+""".r
  
  def parseUrl(url: String): GithubRepository = {
    val repoInfos = url.split(s"$githubUrl/")(1).split("/")
    // TODO : Aller chercher le nb d'issues du dépot
    GithubRepository(None, repoInfos(0), repoInfos(1))
  }

  def getUrl(repo: GithubRepository): String = {
    s"$githubUrl/${repo.owner}/${repo.name}"
  }

}
