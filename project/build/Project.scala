import sbt._


class SwarmProject(info: ProjectInfo) extends DefaultProject(info) with AutoCompilerPlugins {

  val scalatest = "org.scalatest" % "scalatest_2.9.0" % "1.4.1"

  val continuationsPlugin = compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.9.0")

  override def compileOptions = super.compileOptions ++ compileOptions("-P:continuations:enable") ++ compileOptions("-unchecked")

  def getClass(arg: String) =
    if (arg.split(".").size == 0)
      "swarm.demos." + arg
    else arg

  lazy val demo =
    task {
      args =>
        if (args.length > 1)
          actionConstructor(getClass(args(0)), args.toList.tail.toArray)
        else
          task {
            Some("Usage: Listen|PrintABC|ForceRemoteRef|ExplicitMoveTo1 <integer> <string>")
          }
    }

  def actionConstructor(clss: String, args: Array[String]) =
    runTask(Some(clss), testClasspath, args)
}
