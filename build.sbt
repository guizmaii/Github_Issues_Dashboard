name := "GithubDashbord_Backend_Scala"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "com.typesafe.play" %% "play-slick" % "0.6.0.1",
  "org.springframework.security" % "spring-security-crypto" % "3.2.3.RELEASE",
  "com.typesafe.slick" %% "slick" % "2.0.1",
  "net.debasishg" % "redisclient_2.10" % "2.13",
  "com.h2database" % "h2" % "1.3.175" % "test"
)

play.Project.playScalaSettings
