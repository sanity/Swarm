import sbt._
import Keys._

object SwarmTwitterBuild extends Build {
    lazy val core  = Project(id = "core",  base = file("core"))
    lazy val node1 = Project(id = "node1", base = file("node1")) dependsOn(core)
    lazy val node2 = Project(id = "node2", base = file("node2")) dependsOn(core)
}

