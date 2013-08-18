package org.swarmframework.transport

/**
 * A Location tells Swarm where to find referenced data.
 * A Transporter uses a Location to move computation to a remote Swarm node.
 */
trait Location extends Serializable

/**
 * A simple Location implementation which uses TCP/IP.
 */
case class InetLocation(address: java.net.InetAddress, port: Int) extends Location {

  override def equals(other: Any) = {
    other.isInstanceOf[InetLocation] &&
      address == other.asInstanceOf[InetLocation].address &&
      port == other.asInstanceOf[InetLocation].port
  }

  override def hashCode = address.hashCode + 37 * port.hashCode
  
  override def toString = address.toString + ":" + port
}
