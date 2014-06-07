import actors.GithubRepository
import actors.{GithubRepository, GithubActor}
import akka.actor.Props
import play.api._
import play.api.libs.concurrent.Akka

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")

    import play.api.Play.current

//    val repos = List(
//      GithubRepository("junit-team", "junit"),
//      GithubRepository("scala", "scala"),
//      GithubRepository("rails", "rails"),
//      GithubRepository("ruby", "ruby")
//    )

    val repos = List(
      GithubRepository("rails", "rails")
//      GithubRepository("guizmaii", "Github_Issues_Dashboard")
    )

    repos map {
     repo =>
       Akka.system.actorOf(Props[GithubActor]) ! repo
    }

  }


  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
