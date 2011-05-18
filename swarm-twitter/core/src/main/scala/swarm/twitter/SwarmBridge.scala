package swarm.twitter

import swarm._
import data._
import swarm.transport._
import swarm.Swarm.swarm

class SwarmBridge(val port1: Short, val port2: Short) {

  val location1: InetLocation = new InetLocation(java.net.InetAddress.getLocalHost, port1)
  val location2: InetLocation = new InetLocation(java.net.InetAddress.getLocalHost, port2)

  RefMap.locations = List(location1, location2)
  InetTransporter.listen(location1.port)(InetTransporter)

  def swarm(f: () => Any@swarm) = {
    Swarm.spawn(Unit => {
      f()
      NoBee()
    })(InetTransporter)
  }

  def getX(uuid: String) = {
    val location1: InetLocation = new InetLocation(java.net.InetAddress.getLocalHost, port1)
    swarm {
      () =>
        val stringsMap: RefMap[String] = RefMap(classOf[String], "strings")
        val x: Option[String] = stringsMap.get("x")
        Swarm.moveTo(location1)
        Swarm.futureValue(uuid, x)
    }
    Swarm.future(uuid).get
  }

  def updateX(x: String) = {
    val location1: InetLocation = new InetLocation(java.net.InetAddress.getLocalHost, port1)
    swarm {
      () =>
        val stringsMap = RefMap(classOf[String], "strings")
        stringsMap.put(location1, "x", x)
    }
  }
}