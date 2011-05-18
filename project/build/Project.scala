import sbt._

class SwarmProject(info: ProjectInfo) extends ParentProject(info) {

  lazy val swarm_core = project("swarm-core", "swarm-core", new SwarmCoreProject(_))
  lazy val swarm_demos = project("swarm-demos", "swarm-demos", new SwarmDemosProject(_), swarm_core)


  class SwarmCoreProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins {
    lazy val scalaTest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test"
    lazy val cont = compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.0")
    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable") ++ compileOptions("-unchecked")
  }

  class SwarmDemosProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins {
    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable") ++ compileOptions("-unchecked")
  }

}
