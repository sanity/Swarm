package swarm.transport

import swarm.{Logs, Bee}

/**
 * A Transporter moves computation to a remote Swarm node.
 */
trait Transporter {
  def isLocal(location: Location): Boolean

  def transport(f: (Unit => Bee), destination: Location): Unit
}

/**
 * A concrete implementation of SwarmTransporter which uses TCP/IP sockets for communication.
 */
object InetTransporter extends Transporter with Logs {

  import java.net.InetAddress
  import swarm.Swarm

  private[this] val localHost: InetAddress = InetAddress.getLocalHost
  private[this] var _local: Option[InetLocation] = None

  def local: InetLocation = _local.getOrElse(new InetLocation(localHost, 9997))

  override def isLocal(location: Location) = local == location

  override def transport(f: (Unit => Bee), destination: Location) {
    debug("transporting execution to " + destination)
    destination match {
      case InetLocation(address, port) =>
        val skt = new java.net.Socket(address, port);
        val oos = new java.io.ObjectOutputStream(skt.getOutputStream());
        oos.writeObject(f);
        oos.close();
    }
  }

  def listen(port: Short)(implicit tx: Transporter) {
    _local = Some(new InetLocation(localHost, port))

    val server = new java.net.ServerSocket(port);

    var listenThread = new Thread() {
      override def run() = {
        while (true) {
          val socket = server.accept()
          val ois = new java.io.ObjectInputStream(socket.getInputStream())
          val bee = ois.readObject().asInstanceOf[(Unit => Bee)]
          debug("resuming execution from " + local)
          Swarm.continue(bee)
        }
      }
    }
    listenThread.start();
    Thread.sleep(500);
  }
}
