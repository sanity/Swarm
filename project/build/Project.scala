import sbt._

class SwarmProject(info: ProjectInfo) extends ParentProject(info) {

  lazy val swarm_core = project("swarm-core", "swarm-core", new SwarmCoreProject(_))
  lazy val swarm_demos = project("swarm-demos", "swarm-demos", new SwarmDemosProject(_), swarm_core)
  lazy val swarm_twitter = project("swarm-twitter", "swarm-twitter", new SwarmTwitterProject(_))

  class SwarmCoreProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins {
    lazy val scalaTest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test"
    lazy val cont = compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.0")

    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable") ++ compileOptions("-unchecked")
  }

  class SwarmDemosProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins {
    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable")
  }

  class SwarmTwitterProject(info: ProjectInfo) extends ParentProject(info) {
    lazy val swarm_twitter_core = project("core", "swarm-twitter-core", new SwarmTwitterCoreProject(_), swarm_core)
    lazy val swarm_twitter_node1 = project("node1", "swarm-twitter-node1", new SwarmTwitterNode1Project(_), swarm_twitter_core)
    lazy val swarm_twitter_node2 = project("node2", "swarm-twitter-node2", new SwarmTwitterNode2Project(_), swarm_twitter_core)
  }

  class SwarmTwitterCoreProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins {
    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable")
  }

  class SwarmTwitterNode1Project(info: ProjectInfo) extends DefaultWebProject(info) with AutoCompilerPlugins {
    lazy val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test"
    lazy val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"

    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable")
  }

  class SwarmTwitterNode2Project(info: ProjectInfo) extends DefaultWebProject(info) with AutoCompilerPlugins {
    lazy val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test"
    lazy val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"

    override def jettyPort = 8081

    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable")
  }

}
