package swarm

import util.continuations._

object Swarm {
  type swarm = cpsParam[Bee, Bee]
}

/**
 * SwarmExecutor owns all of the continuations code.  Implementations must
 * define transmit(), which sends the continuation to another destination
 */
trait SwarmExecutor {

  import Swarm.swarm

  // To be defined by concrete implementations
  def transmit(f: (Unit => Bee), destination: Location): Unit

  /**
   * Called from concrete implementations to run the continuation
   */
  def continue(f: Unit => Bee) {
    execute(reset(f()))
  }

  /**
   * Start a new Swarm task (will return immediately as task is started in a
   * new thread)
   */
  def spawn(f: Unit => Bee@swarm) {
    val thread = new Thread() {
      override def run() = execute(reset(f()))
    }
    thread.start()
  }

  /**
   * Relocates the code to the given destination
   */
  def moveTo(destination: Location) = shift {
    c: (Unit => Bee) =>
      IsBee(c, destination)
  }

  /**
   * Executes the continuation if it should be run locally, otherwise
   * relocates to the given destination
   */
  def execute(bee: Bee) {
    bee match {
      case IsBee(f, destination) => transmit(f, destination)
      case NoBee() =>
    }
  }
}

/**
 * A concrete implementation of SwarmExecutor which uses sockets for
 * communication.
 */
object InetSwarm extends SwarmExecutor {

  import java.net.InetAddress

  private[this] val localHost: InetAddress = InetAddress.getLocalHost
  private[this] var _local: Option[InetLocation] = None

  def local: InetLocation = _local.getOrElse(new InetLocation(localHost, 9997))

  override def transmit(f: (Unit => Bee), destination: Location) {
    destination match {
      case InetLocation(address, port) =>
        val skt = new java.net.Socket(address, port);
        val oos = new java.io.ObjectOutputStream(skt.getOutputStream());
        oos.writeObject(f);
        oos.close();
    }
  }

  def listen(port: Short) {
    _local = Some(new InetLocation(localHost, port))

    val server = new java.net.ServerSocket(port);

    var listenThread = new Thread() {
      override def run() = {
        while (true) {
          val socket = server.accept()
          val ois = new java.io.ObjectInputStream(socket.getInputStream())
          val bee = ois.readObject().asInstanceOf[(Unit => Bee)]
          continue(bee)
        }
      }
    }
    listenThread.start();
    Thread.sleep(500);
  }
}
