package swarm

import org.scalatest.FunSuite
import util.continuations._
import swarm.Swarm.{Swapped, swarm}

class InMemTest extends FunSuite {

  def executeWith(transporter: Transporter)(f: Unit => Bee@swarm) {
    Swarm.execute(reset(f()))(transporter)
  }

  test("explicit relocate() transports data") {
    InMemTest.currentLocation = None

    executeWith(InMemTest.getTransporter(InMemLocation(1))) {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === None)

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

  test("explicit swap() transports data") {
    InMemTest.currentLocation = None

    executeWith(InMemTest.getTransporter(InMemLocation(1))) {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === None)

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

        // swap the Refs
        val swapped: Swapped[String, String] = Swarm.swap(ref1, ref2)

        // ensure the new Ref takes us to the appropriate location
        assert(swapped.ref1() === "test string one")
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        // ensure the old Ref takes us to the appropriate location
        assert(ref1() === "test string one")
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        // ensure the other new Ref takes us to the appropriate location
        assert(swapped.ref2() === "test string two")
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // ensure the other old Ref takes us to the appropriate location
        assert(ref2() === "test string two")
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        NoBee()
    }
  }

  test("explicit moveTo transports execution") {
    InMemTest.currentLocation = None

    executeWith(InMemTest.getTransporter(InMemLocation(1))) {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === None)

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
    InMemTest.currentLocation = None

    executeWith(InMemTest.getTransporter(InMemLocation(1))) {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === None)

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
  val tx1: InMemTransporter = new InMemTransporter(InMemLocation(1))
  val tx2: InMemTransporter = new InMemTransporter(InMemLocation(2))

  def getTransporter(location: Location): InMemTransporter = location match {
    case InMemLocation(1) => tx1
    case InMemLocation(2) => tx2
  }

  // keeps track of the current in-memory location
  var currentLocation: Option[InMemLocation] = None
}

case class InMemLocation(val id: Int) extends Location

class InMemTransporter(val local: InMemLocation) extends Transporter {
  override def transport(f: Unit => Bee, destination: Location) {
    InMemTest.getTransporter(destination).receive(f)
  }

  def receive(f: Unit => Bee) {
    // update the current in-memory location
    InMemTest.currentLocation = Some(local)
    
    Swarm.continue(f)(this)
  }
}