package swarm.demos

import swarm.transport.InetTransporter

object Listen extends App {
  implicit val tx = InetTransporter
  InetTransporter.listen(9997)
}