package swarm.demos

import swarm.transport.{Transporter, InetTransporter}

object Listen {
  def main(args: Array[String]) = {
    implicit val tx: Transporter = InetTransporter
    InetTransporter.listen(9997)
  }
}