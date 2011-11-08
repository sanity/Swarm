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

    lazy val swarmCore  = Project(id = "swarm-core",    base = file("swarm-core"), settings = coreSettings)
    lazy val swarmDemos = Project(id = "swarm-demos",   base = file("swarm-demos"), settings = baseSettings) dependsOn(swarmCore)
}

object SwarmTwitterBuild extends Build {

    import SwarmBuild.baseSettings
    import SwarmBuild.swarmCore
    import com.github.siasia.WebPlugin.webSettings
    import com.github.siasia.WebPlugin.container
    import com.github.siasia.PluginKeys.port

    val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    val sonatypeNexusReleases  = "Sonatype Nexus Releases"  at "https://oss.sonatype.org/content/repositories/releases"

    val scalatraVersion = "2.0.1"
    val scalatra        = "org.scalatra"      %% "scalatra"      % scalatraVersion
    val auth            = "org.scalatra"      %% "scalatra-auth" % scalatraVersion
    val jetty6          = "org.mortbay.jetty" %  "jetty"         % "6.1.22"        % "container"
    val servletApi      = "javax.servlet"     %  "servlet-api"   % "2.5"           % "provided"

    val swarmTwitterCoreSettings  = baseSettings ++ Seq(libraryDependencies ++= Seq(scalatra, auth, servletApi))
    val swarmTwitterNode1Settings = swarmTwitterCoreSettings  ++ Seq(libraryDependencies ++= Seq(jetty6)) ++ seq(webSettings :_*)
    val swarmTwitterNode2Settings = swarmTwitterNode1Settings ++ Seq(port in container.Configuration := 8081)

    lazy val swarmTwitterCore  = Project(id = "swarm-twitter-core",  base = file("swarm-twitter/core"),  settings = swarmTwitterCoreSettings)  dependsOn(swarmCore)
    lazy val swarmTwitterNode1 = Project(id = "swarm-twitter-node1", base = file("swarm-twitter/node1"), settings = swarmTwitterNode1Settings) dependsOn(swarmTwitterCore)
    lazy val swarmTwitterNode2 = Project(id = "swarm-twitter-node2", base = file("swarm-twitter/node2"), settings = swarmTwitterNode2Settings) dependsOn(swarmTwitterCore)
}

/*

  // old sbt 0.7.7 config to be converted

  lazy val twitterDemo = task {
    swarm_twitter.swarm_twitter_node1.jettyRun.run
    swarm_twitter.swarm_twitter_node2.jettyRun.run
    None
  } dependsOn (swarm_twitter.swarm_twitter_node1.`compile`, swarm_twitter.swarm_twitter_node2.`compile`)

*/

