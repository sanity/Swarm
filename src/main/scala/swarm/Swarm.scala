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

  case class Swapped[A, B](ref1: Ref[A], ref2: Ref[B])

  def swap[A, B](ref1: Ref[A], ref2: Ref[B]): Swapped[A, B]@swarm = {
    val ref1Value = ref1()
    val ref2Value = ref2()

    moveTo(ref1.location)
    Store.remove(ref1.uid) // TODO: don't simply remove the old value -> create a way to reference the new one
    val newRef2 = new Ref[B](ref2.typeClass, ref1.location, Store.save(ref2Value))

    moveTo(ref2.location)
    Store.remove(ref2.uid) // TODO: don't simply remove the old value -> create a way to reference the new one
    val newRef1 = new Ref[A](ref1.typeClass, ref2.location, Store.save(ref1Value))

    Swapped(newRef1, newRef2)
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