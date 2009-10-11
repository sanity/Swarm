package swarm

import java.net._

@serializable class Location(val address : InetAddress, val port : Short) {
	override def equals(other : Any) = {
		if (!other.isInstanceOf[Any]) false;
		val o = other.asInstanceOf[Location];
		address == o.address && port == o.port;
	}
}
