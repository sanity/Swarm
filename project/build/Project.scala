import sbt._

class SwarmProject(info: ProjectInfo) extends ParentProject(info) {

  lazy val swarm_core = project("swarm-core", "swarm-core", new SwarmCoreProject(_))
  lazy val swarm_demos = project("swarm-demos", "swarm-demos", new SwarmDemosProject(_), swarm_core)
  lazy val swarm_twitter = project("swarm-twitter", "swarm-twitter", new SwarmTwitterProject(_))

  trait CompilerOptions extends BasicScalaProject with AutoCompilerPlugins {
    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable") ++ compileOptions("-unchecked")
  }

  class SwarmDefaultProject(info: ProjectInfo) extends DefaultProject(info) with CompilerOptions

  class SwarmDefaultWebProject(info: ProjectInfo) extends DefaultWebProject(info) with CompilerOptions with ScalatraProject

  class SwarmCoreProject(info: ProjectInfo) extends SwarmDefaultProject(info) {
    lazy val scalaTest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test"
    lazy val cont = compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.0")
  }

  class SwarmDemosProject(info: ProjectInfo) extends SwarmDefaultProject(info)

  class SwarmTwitterProject(info: ProjectInfo) extends ParentProject(info) {
    lazy val swarm_twitter_core = project("core", "swarm-twitter-core", new SwarmTwitterCoreProject(_), swarm_core)
    lazy val swarm_twitter_node1 = project("node1", "swarm-twitter-node1", new SwarmTwitterNode1Project(_), swarm_twitter_core)
    lazy val swarm_twitter_node2 = project("node2", "swarm-twitter-node2", new SwarmTwitterNode2Project(_), swarm_twitter_core)
  }

  trait ScalatraProject {
    lazy val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    lazy val sonatypeNexusReleases = "Sonatype Nexus Releases" at "https://oss.sonatype.org/content/repositories/releases"
    lazy val scalatra = "org.scalatra" %% "scalatra" % "2.0.0-SNAPSHOT"
    lazy val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test"
    lazy val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  }

  class SwarmTwitterCoreProject(info: ProjectInfo) extends SwarmDefaultProject(info) with ScalatraProject

  class SwarmTwitterNode1Project(info: ProjectInfo) extends SwarmDefaultWebProject(info)

  class SwarmTwitterNode2Project(info: ProjectInfo) extends SwarmDefaultWebProject(info) {
    override def jettyPort = 8081
  }

}
