name := """Github_Issues_Dashboard"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

resolvers += "spray" at "http://repo.spray.io/"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "net.debasishg" %% "redisclient" % "2.13",
  "io.spray" %%  "spray-json" % "1.2.6"
)
