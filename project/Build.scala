import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "sockets"
  val appVersion      = "1.0"

  val playVersion = "2.2.1"
  val appDependencies = Seq(
    jdbc,
    "com.typesafe.play" %% "play" % playVersion,
    "com.typesafe.play" %% "play-jdbc" % playVersion,
    "com.typesafe.slick" %% "slick" % "1.0.1",
    "javax.servlet" % "javax.servlet-api" % "3.0.1", //needed by org.reflections
    "com.typesafe.play" %% "play-slick" % "0.5.0.8",
    "securesocial" %% "securesocial" % "master-SNAPSHOT",
    "com.github.tototoshi" %% "slick-joda-mapper" % "0.4.0",
    "mysql" % "mysql-connector-java" % "5.1.23",
  )
  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/")) (Resolver.ivyStylePatterns)
  )
}

