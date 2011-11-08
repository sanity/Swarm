import sbt._
import Keys._

object SwarmTwitterBuild extends Build {
    lazy val core  = Project(id = "core",  base = file("core"))
    lazy val node1 = Project(id = "node1", base = file("node1")) dependsOn(core)
    lazy val node2 = Project(id = "node2", base = file("node2")) dependsOn(core)
}

/*

  // old sbt 0.7.7 config to be converted

  lazy val twitterDemo = task {
    swarm_twitter.swarm_twitter_node1.jettyRun.run
    swarm_twitter.swarm_twitter_node2.jettyRun.run
    None
  } dependsOn (swarm_twitter.swarm_twitter_node1.`compile`, swarm_twitter.swarm_twitter_node2.`compile`)

  class SwarmTwitterProject(info: ProjectInfo) extends ParentProject(info) {
    lazy val swarm_twitter_core = project("core", "swarm-twitter-core", new SwarmTwitterCoreProject(_), swarm_core)
    lazy val swarm_twitter_node1 = project("node1", "swarm-twitter-node1", new SwarmTwitterNode1Project(_), swarm_twitter_core)
    lazy val swarm_twitter_node2 = project("node2", "swarm-twitter-node2", new SwarmTwitterNode2Project(_), swarm_twitter_core)
  }

  trait ScalatraProject {
    lazy val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    lazy val sonatypeNexusReleases = "Sonatype Nexus Releases" at "https://oss.sonatype.org/content/repositories/releases"
    lazy val scalatraVersion = "2.0.0-SNAPSHOT"
    lazy val scalatra = "org.scalatra" %% "scalatra" % scalatraVersion
    lazy val auth = "org.scalatra" %% "scalatra-auth" % scalatraVersion
    lazy val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.22" % "test"
    lazy val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  }

  class SwarmTwitterCoreProject(info: ProjectInfo) extends DefaultProject(info) with ScalatraProject with CompilerOptions

  class SwarmTwitterNode1Project(info: ProjectInfo) extends DefaultWebProject(info) with ScalatraProject with CompilerOptions

  class SwarmTwitterNode2Project(info: ProjectInfo) extends DefaultWebProject(info) with ScalatraProject with CompilerOptions {
    override def jettyPort = 8081
  }


*/

