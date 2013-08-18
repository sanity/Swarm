package org.swarmframework.demos

import org.swarmframework.internal._
import org.swarmframework.core.Swarm._
import org.swarmframework.data.Ref
import org.swarmframework.transport._
import java.net.InetAddress

object ForceRemoteRef extends App {
  implicit val tx = InetTransporter
  InetTransporter.listen(9999)

  spawn {
    println("1")
    val vLoc = Ref(InetTransporter.local, "test local string")
    println("2")
    val vRem = Ref(InetLocation(InetAddress.getLocalHost, 9997), "test remote string")
    println("3")
    println(vLoc())
    println("4")
    println(vRem())
    println("5")
    NoBee()
  }
}
