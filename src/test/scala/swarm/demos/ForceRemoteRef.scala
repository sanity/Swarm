package swarm.demos

import swarm.{Swarm, NoBee}
import swarm.data.Ref
import swarm.{Transporter, InetTransporter}

object ForceRemoteRef {
  def main(args: Array[String]) = {
    implicit val tx: Transporter = InetTransporter
    InetTransporter.listen(java.lang.Short.parseShort(args(0)));
    if (args.length > 1 && args(1) == "start") {
      Swarm.spawn(frrThread)
    }
    while (true) {
      Thread.sleep(1000)
    }
  }

  def frrThread(u: Unit) = {
    println("1")
    val vLoc = Ref(InetTransporter.local, "test local string")
    println("2")
    val vRem = Ref(InetTransporter.local, "test remote string")
    println("3")
    println(vLoc())
    println("4")
    println(vRem())
    println("5")
    NoBee()
  }
}