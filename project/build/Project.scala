import sbt._

class SwarmProject(info: ProjectInfo) extends ParentProject(info) {

  lazy val swarm_core = project("swarm-core", "swarm-core", new SwarmCoreProject(_))
  lazy val swarm_demos = project("swarm-demos", "swarm-demos", new SwarmDemosProject(_), swarm_core)
  lazy val swarm_twitter = project("swarm-twitter", "swarm-twitter", new SwarmTwitterProject(_))

  lazy val twitterDemo = task {
    swarm_twitter.swarm_twitter_node1.jettyRun.run
    swarm_twitter.swarm_twitter_node2.jettyRun.run
    None
  } dependsOn (swarm_twitter.swarm_twitter_node1.`compile`, swarm_twitter.swarm_twitter_node2.`compile`)

  trait CompilerOptions extends BasicScalaProject with AutoCompilerPlugins {
    override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable") ++ compileOptions("-unchecked")
  }

  class SwarmCoreProject(info: ProjectInfo) extends DefaultProject(info) with CompilerOptions {
    lazy val scalaTest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1" % "test"
    lazy val log4j = "log4j" % "log4j" % "1.2.16"
    lazy val cont = compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.0")
  }

  class SwarmDemosProject(info: ProjectInfo) extends DefaultProject(info) with CompilerOptions {
    lazy val listen = task {
      args =>
        val forkConfiguration = new ForkScalaRun {
          override def workingDirectory = Some(info.projectPath.asFile)

          override def scalaJars = buildScalaInstance.libraryJar ::
            buildScalaInstance.compilerJar :: Nil
        }
        val forkRun = new ForkRun(forkConfiguration)
        runTask(Some("swarm.demos.Listen"), runClasspath, args)(forkRun)
    }
  }

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

  class SwarmTwitterCoreProject(info: ProjectInfo) extends DefaultProject(info) with ScalatraProject with CompilerOptions

  class SwarmTwitterNode1Project(info: ProjectInfo) extends DefaultWebProject(info) with ScalatraProject with CompilerOptions

  class SwarmTwitterNode2Project(info: ProjectInfo) extends DefaultWebProject(info) with ScalatraProject with CompilerOptions {
    override def jettyPort = 8081
  }

}
