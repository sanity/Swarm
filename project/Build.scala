import sbt._
import sbt.Defaults.{defaultSettings => default}
import Keys._

object SwarmBuild extends Build {

  // Build settings
  val scalaVer = "2.10.1"
  val customResolvers = Seq(
    "Sonatype OSS Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
    "Akka Repo" at "http://repo.akka.io/repository"
  )
  version := "0.1-SNAPSHOT"
  name := "swarm"
  organization := "org.swarmframework"

  // Swarm Dependencies
  val scalatest       = "org.scalatest"     %% "scalatest"       % "1.9.1"  % "test"
  // Demo Dependencies
  val scalatra        = "org.scalatra"      %% "scalatra"        % "2.2.1"
  val jetty6          = "org.mortbay.jetty" %  "jetty"           % "6.1.22"
  val servletApi      = "javax.servlet"     %  "servlet-api"     % "2.5"
  val logback         = "ch.qos.logback"    % "logback-classic"  % "1.0.11"

  // Delimited Continuations Plugin
  val cps = Seq(
    autoCompilerPlugins := true,
    addCompilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.1"),
    scalacOptions += "-P:continuations:enable"
  )

  lazy val core  = Project(
    id = "core",
    base = file("swarm-core"),
    settings = default ++ cps ++ Seq(
      scalaVersion := scalaVer,
      libraryDependencies ++= Seq(scalatest, logback)
    )
  )

  lazy val demos = Project(
    id = "demos",
    base = file("swarm-demos"),
    dependencies = Seq(core),
    settings = default ++ cps ++ Seq(
      scalaVersion := scalaVer,
      resolvers ++= customResolvers,
      libraryDependencies ++= Seq(scalatest, scalatra, jetty6, servletApi, logback)
    )
  )
}

