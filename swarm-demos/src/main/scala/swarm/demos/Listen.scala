package org.swarmframework.demos

import org.swarmframework.transport.InetTransporter

object Listen extends App {
  implicit val tx = InetTransporter
  InetTransporter.listen(9997)
}