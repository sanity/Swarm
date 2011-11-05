import sbt._
import Keys._

object SwarmBuild extends Build {

    val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1"  % "test"
    val log4j     = "log4j"         % "log4j"           % "1.2.16"

    val baseSettings = Defaults.defaultSettings ++ Seq(
      autoCompilerPlugins := true,
      addCompilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.1"),
      scalacOptions += "-P:continuations:enable"
    )

    val coreSettings = baseSettings ++ Seq(
      libraryDependencies ++= Seq(scalatest, log4j)
    )

    lazy val root    = Project(id = "swarm",         base = file(".")) aggregate(core, demos, twitter)
    lazy val core    = Project(id = "swarm-core",    base = file("swarm-core"), settings = coreSettings)
    lazy val demos   = Project(id = "swarm-demos",   base = file("swarm-demos"), settings = baseSettings) dependsOn(core)
    lazy val twitter = Project(id = "swarm-twitter", base = file("swarm-twitter"), settings = baseSettings) dependsOn(core)
}

