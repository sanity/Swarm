package swarm

@serializable trait Location

case class InetLocation(val address: java.net.InetAddress, val port: Short) extends Location {
	override def equals(other: Any) = {
		if (!other.isInstanceOf[Any]) false
		val o = other.asInstanceOf[InetLocation]
		address == o.address && port == o.port
	}
}
