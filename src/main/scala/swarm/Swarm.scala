package swarm

import util.continuations._

trait Transporter {
  def transport(f: (Unit => Bee), destination: Location): Unit
}

/**
 * Swarm owns all of the continuations code. It relies on an implicit
 * SwarmTransporter, which defines how continuations are transported
 * between nodes.
 */
object Swarm {

  type swarm = cpsParam[Bee, Bee]

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
  def spawn(f: Unit => Bee@swarm)(implicit tx: Transporter) {
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

  def relocate[A](ref: Ref[A], destination: Location): Ref[A]@swarm = {
    val refValue = ref()

    moveTo(destination)
    val newRef = new Ref[A](ref.typeClass, destination, Store.save(refValue))

    moveTo(ref.location)
    Store.update(ref.uid, newRef)

    newRef
  }

  case class Swapped[A, B](ref1: Ref[A], ref2: Ref[B])

  def swap[A, B](ref1: Ref[A], ref2: Ref[B]): Swapped[A, B]@swarm = {
    Swapped(relocate(ref1, ref2.location), relocate(ref2, ref1.location))
  }

  /**
   * Executes the continuation if it should be run locally, otherwise
   * relocates to the given destination
   */
  def execute(bee: Bee)(implicit tx: Transporter) {
    bee match {
      case IsBee(f, destination) => tx.transport(f, destination)
      case NoBee() =>
    }
  }
}