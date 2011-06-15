package swarm

import org.scalatest.FunSuite
import util.continuations._
import swarm.Swarm.swarm
import swarm.data.Ref
import swarm.transport.{Location, Transporter}

class InMemTest extends FunSuite {

  def execute(f: Unit => Bee@swarm) {
    Swarm.execute(reset(f()))(InMemTest.getTransporter)
  }

  test("dsl should change location and run execution") {
    import swarm.Swarm.at

    implicit val local: Location = InMemLocation(1)

    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

        assert(InMemTest.currentLocation === Some(InMemLocation(1)))
        at(InMemLocation(2)) run {
          assert(InMemTest.currentLocation === Some(InMemLocation(2)))
        }

        NoBee()
    }
  }

  // TODO this fails sometimes, then passes again without any changes
  test("explicit relocate() transports data") {
    implicit val local: Location = InMemLocation(1)

    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // create a Ref
        val ref = Ref(InMemLocation(1), "test string one")
        assert(ref() === "test string one")
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // relocate the Ref
        val newRef = Swarm.relocate(ref, InMemLocation(2))
        assert(newRef() === "test string one")
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        NoBee()
    }
  }

  test("explicit moveTo transports execution") {
    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // move to a location
        Swarm.moveTo(InMemLocation(2))
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        // move to another location
        Swarm.moveTo(InMemLocation(1))
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        NoBee()
    }
  }

  test("ref access transports execution") {
    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // create a Ref
        val ref1 = Ref(InMemLocation(1), "test string one")
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // create another Ref
        val ref2 = Ref(InMemLocation(2), "test string two")
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        // ensure the Ref takes us to the appropriate location
        assert(ref1() === "test string one")
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // ensure the other Ref takes us to the appropriate location
        assert(ref2() === "test string two")
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        NoBee()
    }
  }
}

object InMemTest {
  val tx0: InMemTransporter = new InMemTransporter(InMemLocation(0))
  val tx1: InMemTransporter = new InMemTransporter(InMemLocation(1))
  val tx2: InMemTransporter = new InMemTransporter(InMemLocation(2))

  def getTransporter(location: Location): InMemTransporter = location match {
    case InMemLocation(1) => tx1
    case InMemLocation(2) => tx2
  }

  def getTransporter: InMemTransporter = currentLocation match {
    case Some(tx) => getTransporter(tx)
    case None => tx0
  }

  // keeps track of the current in-memory location
  var currentLocation: Option[InMemLocation] = None
}

case class InMemLocation(val id: Int) extends Location

class InMemTransporter(val local: InMemLocation) extends Transporter {

  override def isLocal(location: Location) = local == location

  override def transport(f: Unit => Bee, destination: Location) {
    InMemTest.getTransporter(destination).receive(f)
  }

  def receive(f: Unit => Bee) {
    // update the current in-memory location
    InMemTest.currentLocation = Some(local)

    Swarm.continue(f)(this)
  }
}