package swarm.demos

import swarm.transport.{Transporter, InetTransporter, InetLocation}
import swarm.{Bee, NoBee, Swarm}
import swarm.Swarm.swarm
import swarm.data.Ref

object PrintABC {
  def main(args: Array[String]) = {
    implicit val tx: Transporter = InetTransporter
    InetTransporter.listen(9998);
    Swarm.spawn(printABC(args(0).toShort))
  }

  def printABC(remotePort: Short)(u: Unit): Bee@swarm = {
    val local = new InetLocation(java.net.InetAddress.getLocalHost, 9998)
    val remote = new InetLocation(java.net.InetAddress.getLocalHost, remotePort)

    val a = Ref(local, "bumble bee")
    val b = Ref(local, "honey bee")
    val c = Ref(remote, "stingless bee")

    println(a())
    println(b())
    println(c())

    NoBee()
  }
}