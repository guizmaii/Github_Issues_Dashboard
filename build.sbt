name := """Github_Issues_Dashboard"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

resolvers += "spray" at "http://repo.spray.io/"

resolvers += Resolver.url("github repo for html5tags", url("http://loicdescotte.github.io/Play2-HTML5Tags/releases/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "net.debasishg" %% "redisclient" % "2.13",
  "io.spray" %%  "spray-json" % "1.2.6",
  "com.typesafe.play" %% "play-slick" % "0.7.0-M1"
)
