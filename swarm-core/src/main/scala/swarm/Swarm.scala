package swarm

import data.{Store, Ref}
import transport._
import util.continuations._
import java.util.concurrent.Executors
import collection.IterableLike
import collection.generic.CanBuildFrom

/**
 * Swarm owns all of the continuations code. It relies on an implicit
 * SwarmTransporter, which defines how continuations are transported
 * between nodes.
 */
object Swarm {

  type swarm = cpsParam[Bee, Bee]

  val executor = Executors.newFixedThreadPool(10)

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

  private class Future(private var _value: Any) {

    def value = _value

    def value_=(value: Any) {
      synchronized {
        _value = value
        notify
      }
    }

    def get: Any = {
      synchronized {
        wait
        value
      }
    }
  }

  private[this] val futures = new collection.mutable.HashMap[String, Future]()

  def saveFutureResult(uuid: String, value: Any) = getFuture(uuid).value = value

  def getFutureResult(uuid: String) = getFuture(uuid).get

  private def getFuture(uuid: String) = {
    if (!futures.contains(uuid)) {
      futures(uuid) = new Future(uuid)
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
}
