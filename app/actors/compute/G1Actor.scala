package actors.compute

import akka.actor.{PoisonPill, Actor}
import actors.{GithubRepository, ParsedRepositoryData, Redisable}
import play.api.Logger
import org.joda.time.DateTime
import scala.collection.mutable
import org.joda.time.format.DateTimeFormat
import java.util.Locale
import domain.{G1, GraphType, GithubIssue}

case class G1ComputedData(repo: GithubRepository, computedData: mutable.Map[String, Int], graphType: GraphType = G1)

class G1Actor extends Actor with Redisable {

  val fmt = DateTimeFormat.forPattern("yyyyMMdd")
  val frenchFmt = fmt.withLocale(Locale.FRENCH)

  val graphPoints = mutable.Map[String, Int]()

  override def receive: Receive = {

    case parsedRepo: ParsedRepositoryData =>
      parsedRepo.parsedData map {
        issue =>
          val createdDate = DateTime.parse(issue.created_at)
          graphPoints(frenchFmt.print(createdDate)) = countOpenIssues(createdDate, parsedRepo.parsedData)
      }
      redisActor ! G1ComputedData(parsedRepo.repo, graphPoints)
      // TODO : Réfléchir : Ici, pas de PoisonPil car c'est un singleton qui fera les calculs pour tous les dépots. Voir l'acteur parseur.

    case error: Exception =>
      Logger.error(s"${this.getClass} | ERROR : ${error.getMessage}")
      // TODO : Valider l'utiliter de s'envoyer une PoisonPill
      self ! PoisonPill
      throw error
  }

  /**
   * Retourne le nombre d'issues ouverte à une date donnée.
   *
   * @param date: la date donnée
   * @param issues: l'ensemble des issues à analyser
   * @return
   */
  private def countOpenIssues(date: DateTime, issues: List[GithubIssue]): Int = {
    var cpt = 0
    issues map {
      issue =>
      if (issue.isOpenAtThisDate(date)) {
        cpt += 1
      }
    }
    cpt
  }

}
