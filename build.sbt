name := """Github_Issues_Dashboard"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

resolvers += "spray" at "http://repo.spray.io/"

resolvers += Resolver.url("github repo for html5tags", url("http://loicdescotte.github.io/Play2-HTML5Tags/releases/"))(Resolver.ivyStylePatterns)

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "mysql" % "mysql-connector-java" % "5.1.31",
  "net.debasishg" %% "redisclient" % "2.13",
  "io.spray" %%  "spray-json" % "1.2.6", // Les versions de spray et de spray sont décorrélées
  "io.spray" % "spray-client" % "1.3.1",
  "com.typesafe.play" %% "play-slick" % "0.7.0-M1",
  "joda-time" % "joda-time" % "2.3",
  "com.mohiva" %% "play-silhouette" % "1.0",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta4", // Needed by Silhouette
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "jquery" % "1.11.1",
  "org.webjars" % "bootswatch-readable" % "3.1.1",
  "org.webjars" % "d3js" % "3.4.8",
  "org.webjars" % "nvd3" % "1.1.15-beta",
  "org.webjars" % "sugar" % "1.4.1",
  "org.webjars" % "font-awesome" % "4.1.0",
  "org.webjars" % "momentjs" % "2.7.0",
  "org.webjars" % "underscorejs" % "1.6.0", // Needed by moment-duration-format
  "org.webjars" % "lodash" % "2.4.1" // Needed by moment-duration-format
)
