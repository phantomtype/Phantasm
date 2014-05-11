import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "phantasm"
  val appVersion      = "1.0"

  val playVersion = "2.2.1"
  val appDependencies = Seq(
    jdbc,
    "com.typesafe.play" %% "play" % playVersion,
    "com.typesafe.play" %% "play-jdbc" % playVersion,
    "com.typesafe.slick" %% "slick" % "2.0.1",
    "org.slf4j" % "slf4j-nop" % "1.6.4",
    "javax.servlet" % "javax.servlet-api" % "3.0.1", //needed by org.reflections
    "com.typesafe.play" %% "play-slick" % "0.6.0.1",
//    "ws.securesocial" %% "securesocial" % "2.1.3",
    "securesocial" %% "securesocial" % "master-SNAPSHOT",
    "joda-time" % "joda-time" % "2.3",
    "org.joda" % "joda-convert" % "1.5",
    "com.github.tototoshi" %% "slick-joda-mapper" % "1.0.1",
    "mysql" % "mysql-connector-java" % "5.1.29"
  )
  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("sbt-plugin-snapshots", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-snapshots/")) (Resolver.ivyStylePatterns)
  )
}

