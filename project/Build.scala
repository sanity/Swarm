import sbt._
import sbt.Defaults.{defaultSettings => default}
import Keys._

object SwarmBuild extends Build {

  val log4j           = "log4j"             %  "log4j"           % "1.2.16"
  val scalatra        = "org.scalatra"      %% "scalatra"        % "2.0.1"
  val auth            = "org.scalatra"      %% "scalatra-auth"   % "2.0.1"
  val jetty6          = "org.mortbay.jetty" %  "jetty"           % "6.1.22"
  val servletApi      = "javax.servlet"     %  "servlet-api"     % "2.5"
  val scalatest       = "org.scalatest"     %  "scalatest_2.9.0" % "1.4.1"  % "test"

  val cps = Seq(autoCompilerPlugins := true,
              addCompilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.1"),
              scalacOptions += "-P:continuations:enable")


  lazy val core  = Project(
    id = "core",
    base = file("swarm-core"),
    settings = default ++ cps ++ Seq(
      libraryDependencies ++= Seq(scalatest, log4j)
    )
  )

  lazy val demos = Project(
    id = "demos",
    base = file("swarm-demos"),
    dependencies = Seq(core),
    settings = default ++ cps ++ Seq(
      libraryDependencies ++= Seq(scalatest, log4j, scalatra, auth, jetty6, servletApi)
    )
  )
}

