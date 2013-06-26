import sbt._
import Keys._

object Settings {
  val name = "swaggerkit"

  val buildSettings = Project.defaultSettings ++ Seq(
    version := "0.2.1-SNAPSHOT",
    scalaVersion := "2.10.0",
    organization := "net.eamelink",
    scalacOptions ++= Seq("-deprecation", "-feature")
  )
}

object Resolvers {
  val typesafeRepo = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  val sprayRepo    = "spray repo"          at "http://repo.spray.io"
}

object Dependencies {
  lazy val play = "play" %% "play" % "2.1.0"
  lazy val specs = "org.specs2" %% "specs2" % "1.13" % "test"
  lazy val spray_json = "io.spray" %% "spray-json" % "1.2.5"
}

object ApplicationBuild extends Build {
  import Settings._
  import Resolvers._
  import Dependencies._

  lazy val root = Project(name, file("."), settings = buildSettings ++ Seq(
    publish := {})
  ) aggregate (core, play2, spray)

  lazy val core = Project(name + "-core", file("core"), settings = buildSettings ++ Seq(
    publishTo <<= version { (v: String) =>
      val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
      Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
    },
    resolvers := Seq(typesafeRepo),
    libraryDependencies ++= Seq(specs)))

  lazy val play2 = Project(name + "-play2", file("play2"), settings = buildSettings ++ Seq(
    publishTo <<= version { (v: String) =>
      val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
      Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
    },
    resolvers := Seq(typesafeRepo),
    libraryDependencies ++= Seq(play, specs))) dependsOn core

  lazy val spray = Project(name + "-spray", file("spray"), settings = buildSettings ++ Seq(
    publishTo <<= version { (v: String) =>
      val path = if(v.trim.endsWith("SNAPSHOT")) "snapshots-public" else "releases-public"
      Some(Resolver.url("Lunatech Artifactory", new URL("http://artifactory.lunatech.com/artifactory/%s/" format path)))
    },
    resolvers := Seq(sprayRepo),
    libraryDependencies ++= Seq(spray_json, specs))) dependsOn core

}
