package swarm

import util.continuations._

/**
 * SwarmExecutor owns all of the continuations code.  Implementations must
 * define isLocal() and transmit().  isLocal() determines whether the given
 * location corresponds to this instance of SwarmExecutor, and transmit() sends
 * the continuation to another destination
 */
trait SwarmExecutor {

  // To be defined by concrete implementations
  def isLocal(location: Location): Boolean

  def transmit(f: (Unit => Bee), destination: Location): Unit

  type swarm = cpsParam[Bee, Bee]

  /**
   * Called from concrete implementations to run the continuation
   */
  def continue(bee: (Unit => Bee)) = {
    val f: (Unit => Bee@swarm) = ((Unit) => shiftUnit(bee()))
    execute(reset {
      f()
    })
  }

  /**
   * Start a new Swarm task (will return immediately as task is started in a
   * new thread)
   */
  def spawn(f: Unit => Bee@swarm) = {
    val thread = new Thread() {
      override def run() = {
        execute(reset {
          f()
          NoBee()
        })
      }
    }
    thread.start()
  }

  /**
   * Relocates the code to the given destination
   */
  def moveTo(destination: Location) = shift {
    c: (Unit => Bee) => {
      IsBee(c, destination)
    }
  }

  /**
   * Executes the continuation if it should be run locally, otherwise
   * relocates to the given destination
   */
  def execute(bee: Bee) = {
    bee match {
      case IsBee(f, destination) if isLocal(destination) => f()
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

  def localHost: java.net.InetAddress = java.net.InetAddress.getLocalHost

  private[this] var _local: Option[InetLocation] = None

  def local: InetLocation = _local.getOrElse(new InetLocation(localHost, 9997))

  override def isLocal(location: Location): Boolean = local == location

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
