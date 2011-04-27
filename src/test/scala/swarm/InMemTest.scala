package swarm

import org.scalatest.FunSuite
import util.continuations._
import swarm.Swarm.{Swapped, swarm}

class InMemTest extends FunSuite {

  def execute(f: Unit => Bee@swarm) {
    Swarm.execute(reset(f()))(InMemTest.getTransporter)
  }

  test("remote access should be tracked") {
    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // create a Ref
        val ref1 = Ref(InMemLocation(1), "test string one")
        assert(ref1() === "test string one")
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // ensure the Ref access was local/not tracked
        assert(Swarm.getDemand(ref1) === 0)

        // create a new Ref
        val ref2 = Ref(InMemLocation(2), "test string two")
        assert(ref2() === "test string two")
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        // ensure the new Ref access was local/not tracked
        assert(Swarm.getDemand(ref2) === 0)

        // access the new Ref a few times
        assert(ref2() === "test string two")
        assert(ref2() === "test string two")
        assert(ref2() === "test string two")
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        // ensure the new Ref accesses were local/not tracked
        assert(Swarm.getDemand(ref2) === 0)

        // access the Ref
        assert(ref1() === "test string one")

        // ensure the Ref access was remote/tracked
        assert(Swarm.getDemand(ref1) === 1)
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // access the Ref a few times
        assert(ref1() === "test string one")
        assert(ref1() === "test string one")
        assert(ref1() === "test string one")

        // ensure the Ref accesses were local/not tracked
        assert(Swarm.getDemand(ref1) === 1)
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // access the new Ref
        assert(ref2() === "test string two")

        // ensure the Ref access was remote/tracked
        assert(Swarm.getDemand(ref2) === 1)
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        // access the Ref a few times
        assert(ref2() === "test string two")
        assert(ref2() === "test string two")
        assert(ref2() === "test string two")

        // ensure the Ref accesses were local/not tracked
        assert(Swarm.getDemand(ref2) === 1)
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        NoBee()
    }
  }

  test("explicit relocate() transports data") {
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

  test("explicit swap() transports data") {
    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

      // start with a clean slate
        assert(InMemTest.currentLocation === Some(InMemLocation(1)))

        // create a Ref
        val ref1 = Ref(InMemLocation(1), "test string one")
        val ref1Clone = new Ref(ref1.typeClass, ref1.location, ref1.uid)
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

        // ensure that "other" references get updated
        ref1Clone()
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