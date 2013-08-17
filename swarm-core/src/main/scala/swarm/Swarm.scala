package swarm

import data.{Store, Ref}
import transport._
import util.continuations._
import java.util.concurrent.{ThreadPoolExecutor, TimeUnit, LinkedBlockingQueue, ConcurrentHashMap}
import java.util.UUID
import scala.collection.JavaConverters._

/**
 * Swarm owns all of the continuations code. It relies on an implicit
 * SwarmTransporter, which defines how continuations are transported
 * between nodes.
 */
object Swarm {

  type swarm = cpsParam[Bee, Bee]

  /**
   * Mix behavior of fixedThreadPool and cachedThreadPool
   * to achieve maximum performance. Creates threads as needed
   * up until the maximum of 20 threads per core, kills idle threads
   * after 60 seconds of inactivity, reuses threads if available, and
   * will not reject thread creation if max limit is reached (those
   * tasks will be queued until a thread is available)
   */
  private val processorCount = Runtime.getRuntime.availableProcessors
  val executor = new ThreadPoolExecutor(
    // Min # of threads
    processorCount,
    // Max of 20 threads per core
    processorCount * 20,
    // Kill extra (more than processorCount) threads after 60 seconds of inactivity
    60L, TimeUnit.SECONDS,
    // Queue type
    new LinkedBlockingQueue[Runnable]()
  )

  /**
   * Called from concrete implementations to run the continuation
   */
  def continue(f: Unit => Bee)(implicit tx: Transporter) {
    execute(reset(f()))
  }

  /**
   * Start a new Swarm task (will return immediately as task is started in a
   * new thread)
   */
  def spawn(f: => Any@swarm)(implicit tx: Transporter) {
    val runnable = new Runnable() {
      override def run() = execute(reset {
        f
        NoBee()
      })
    }
    executor.execute(runnable)
  }

  def promise[A](uuid: String, f: => A@swarm)(implicit local: Location) = {
    futures(uuid) = new Future
    val x = f
    Swarm.moveTo(local)
    Swarm.saveFutureResult(uuid, x)
  }


  /**
   * Relocates the code to the given destination
   */
  def moveTo(destination: Location) = shift {
    c: (Unit => Bee) =>
      IsBee(c, destination)
  }

  def relocate[A](ref: Ref[A], destination: Location): Ref[A]@swarm = {
    val refValue = ref()

    moveTo(destination)
    val newRef = new Ref[A](ref.typeClass, destination, Store.save(refValue))

    moveTo(ref.location)
    Store.relocate(ref.uid, newRef)
    ref.relocate(newRef.uid, newRef.location)

    newRef
  }

  def dereference(ref: Ref[_]) = shift {
    c: (Unit => Bee) =>
      RefBee(c, ref)
  }

  /**
   * Executes the continuation if it should be run locally, otherwise
   * relocates to the given destination
   */
  def execute(bee: Bee)(implicit tx: Transporter) {
    bee match {
      case RefBee(f, ref) if (tx.isLocal(ref.location)) =>
        if (!Store.exists(ref.uid)) {
          val newRef = Store.relocated(ref.uid)
          ref.relocate(newRef.uid, newRef.location)
          tx.transport(f, ref.location)
        } else {
          Swarm.continue(f)
        }
      case RefBee(f, ref) => tx.transport(f, ref.location) // TODO track remote dereferences to use in balancing/redistributing data
      case IsBee(f, destination) if (tx.isLocal(destination)) => Swarm.continue(f)
      case IsBee(f, destination) => tx.transport(f, destination)
      case NoBee() =>
    }
  }

  private class Future(private var _value: Any = null) {

    def value = _value

    def value_=(theValue: Any) {
      synchronized {
        _value = theValue
        notify
      }
    }

    def get: Any = {
      synchronized {
        if (_value == null) wait
        value
      }
    }
  }

  private[this] val futures = new ConcurrentHashMap[String, Future]() asScala

  def saveFutureResult(uuid: String, value: Any) = getFuture(uuid).value = value

  def getFutureResult(uuid: String) = getFuture(uuid).get

  private def getFuture(uuid: String) = synchronized {
    if (!futures.contains(uuid)) {
      futures(uuid) = new Future()
    }
    futures(uuid)
  }

  def foreach[A](xs: List[A], f: A => Unit@swarm): Unit@swarm = {
    xs match {
      case Nil =>
      case x :: moreXs =>
        f(x)
        foreach(moreXs, f)
    }
  }

  def at(location: Location) = new {
    def run(f: => Any@swarm): Any@swarm = {
      moveTo(location)
      f
    }
  }

  def remember(f: => Any@swarm)(implicit tx: Transporter, local: Location): String = {
    val uuid = UUID.randomUUID.toString

    Swarm.spawn {
      val x = f
      Swarm.moveTo(local)
      Swarm.saveFutureResult(uuid, x)
    }

    uuid
  }
}
