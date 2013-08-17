package swarm

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers._
import util.continuations._
import swarm.data.Ref
import swarm.transport.{Location, Transporter}
import swarm.Swarm._

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

        Some(InMemLocation(1)) should equal(InMemTest.currentLocation)

        at(InMemLocation(2)) run {
          Some(InMemLocation(2)) should equal(InMemTest.currentLocation)
        }

        NoBee()
    }
  }

  test("dsl should save future value") {
    import swarm.Swarm.remember

    implicit val local: Location = InMemLocation(1)
    implicit val tx: Transporter = new InMemTransporter(InMemLocation(1))

    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

        Some(InMemLocation(1)) should equal(InMemTest.currentLocation)
        val uuid = remember {
          moveTo(InMemLocation(2))
          1
        }

        moveTo(local)
        getFutureResult(uuid) should equal(1)

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
        InMemTest.currentLocation should equal(Some(InMemLocation(1)))

        // create a Ref
        val ref = Ref(InMemLocation(1), "test string one")
        val refResult = ref()
        refResult should equal("test string one")
        InMemTest.currentLocation should equal(Some(InMemLocation(1)))

        // relocate the Ref
        val newRef = relocate(ref, InMemLocation(2))
        val newRefResult = newRef()
        newRefResult should equal("test string one")
        InMemTest.currentLocation should equal(Some(InMemLocation(2)))

        NoBee()
    }
  }

  test("explicit moveTo transports execution") {
    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

        // start with a clean slate
        InMemTest.currentLocation should equal(Some(InMemLocation(1)))

        // move to a location
        moveTo(InMemLocation(2))
        InMemTest.currentLocation should equal(Some(InMemLocation(2)))

        // move to another location
        moveTo(InMemLocation(1))
        InMemTest.currentLocation should equal(Some(InMemLocation(1)))

        NoBee()
    }
  }

  test("ref access transports execution") {
    InMemTest.currentLocation = Some(InMemLocation(1))

    execute {
      Unit =>

      // start with a clean slate
        InMemTest.currentLocation should equal(Some(InMemLocation(1)))

        // create a Ref
        val ref1 = Ref(InMemLocation(1), "test string one")
        InMemTest.currentLocation should equal(Some(InMemLocation(1)))

        // create another Ref
        val ref2 = Ref(InMemLocation(2), "test string two")
        InMemTest.currentLocation should equal(Some(InMemLocation(2)))

        // ensure the Ref takes us to the appropriate location
        val ref1result = ref1()
        ref1result should equal("test string one")
        InMemTest.currentLocation should equal(Some(InMemLocation(1)))

        // ensure the other Ref takes us to the appropriate location
        val ref2result = ref2()
        ref2result should equal("test string two")
        InMemTest.currentLocation should equal(Some(InMemLocation(2)))

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