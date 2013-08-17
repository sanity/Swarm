package swarm.demos

import swarm.transport.{InetTransporter, InetLocation}
import swarm.NoBee
import swarm.Swarm._
import swarm.data.Ref
import java.net.InetAddress

object PrintABC extends App {
  implicit val tx = InetTransporter
  InetTransporter.listen(9998);

  spawn {
    val local = InetLocation(InetAddress.getLocalHost, 9998)
    val remote = InetLocation(InetAddress.getLocalHost, 9997)

    val a = Ref(local, "bumble bee")
    val b = Ref(local, "honey bee")
    val c = Ref(remote, "stingless bee")

    println(a())
    println(b())
    println(c())

    NoBee()
  }
}