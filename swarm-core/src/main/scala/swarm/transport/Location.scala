package swarm.transport

/**
 * A Location tells Swarm where to find referenced data.
 * A Transporter uses a Location to move computation to a remote Swarm node.
 */
trait Location extends Serializable

/**
 * A simple Location implementation which uses TCP/IP.
 */
case class InetLocation(val address: java.net.InetAddress, val port: Short) extends Location {
  override def equals(other: Any) = {
    if (!other.isInstanceOf[Any]) false
    val o = other.asInstanceOf[InetLocation]
    address == o.address && port == o.port
  }
}
