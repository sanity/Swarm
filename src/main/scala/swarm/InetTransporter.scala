package swarm

/**
 * A concrete implementation of SwarmTransporter which uses sockets for
 * communication.
 */
object InetTransporter extends Transporter {

  import java.net.InetAddress

  private[this] val localHost: InetAddress = InetAddress.getLocalHost
  private[this] var _local: Option[InetLocation] = None

  def local: InetLocation = _local.getOrElse(new InetLocation(localHost, 9997))

  override def transport(f: (Unit => Bee), destination: Location) {
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
          Swarm.continue(bee)
        }
      }
    }
    listenThread.start();
    Thread.sleep(500);
  }
}
