import actors.{GithubRepository, GithubActor}
import akka.actor.Props
import play.api._
import play.api.libs.concurrent.Akka

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    Logger.info("Application has started")

    import play.api.Play.current

    val githubActor = Akka.system.actorOf(Props[GithubActor])

    githubActor ! GithubRepository("junit-team", "junit")
//    githubActor ! Repository("scala", "scala")

  }


  override def onStop(app: Application) {
    Logger.info("Application shutdown...")
  }

}
