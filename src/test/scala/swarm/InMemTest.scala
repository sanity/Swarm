package swarm

import org.scalatest.FunSuite
import util.continuations._
import swarm.Swarm.{Swapped, swarm}

class InMemTest extends FunSuite {

  test("explicit relocate transports data") {
    InMemTest.currentLocation = None

    Swarm.execute(reset(imrt()))(InMemTest.getSwarm(InMemLocation(1)))

    def imrt(u: Unit): Bee@swarm = {
      assert(InMemTest.currentLocation === None)

      val ref = Ref(InMemLocation(1), "test string one")
      assert(ref() === "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      val newRef = Swarm.relocate(ref, InMemLocation(2))
      
      assert(newRef() === "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      NoBee()
    }
  }

  test("explicit swap transports data") {
    InMemTest.currentLocation = None

    Swarm.execute(reset(imrt()))(InMemTest.getSwarm(InMemLocation(1)))

    def imrt(u: Unit): Bee@swarm = {
      assert(InMemTest.currentLocation === None)

      val ref1 = Ref(InMemLocation(1), "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      val ref2 = Ref(InMemLocation(2), "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      assert(ref1() === "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      assert(ref2() === "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      val swapped: Swapped[String, String] = Swarm.swap(ref1, ref2)

      assert(swapped.ref1() === "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      assert(swapped.ref2() === "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      NoBee()
    }
  }
  
  test("explicit moveTo transports execution") {
    InMemTest.currentLocation = None

    Swarm.execute(reset(imst()))(InMemTest.getSwarm(InMemLocation(1)))

    def imst(u: Unit): Bee@swarm = {
      assert(InMemTest.currentLocation === None)

      Swarm.moveTo(InMemLocation(2))
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      Swarm.moveTo(InMemLocation(1))
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      NoBee()
    }
  }

  test("ref access transports execution") {
    InMemTest.currentLocation = None

    Swarm.execute(reset(imrt()))(InMemTest.getSwarm(InMemLocation(1)))

    def imrt(u: Unit): Bee@swarm = {
      assert(InMemTest.currentLocation === None)

      val ref1 = Ref(InMemLocation(1), "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      val ref2 = Ref(InMemLocation(2), "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      assert(ref1() === "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      assert(ref2() === "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      NoBee()
    }
  }
}

object InMemTest {
  val tx1: InMemTransporter = new InMemTransporter(InMemLocation(1))
  val tx2: InMemTransporter = new InMemTransporter(InMemLocation(2))

  def getSwarm(location: Location): InMemTransporter = location match {
    case InMemLocation(1) => tx1
    case InMemLocation(2) => tx2
  }

  var currentLocation: Option[InMemLocation] = None
}

case class InMemLocation(val id: Int) extends Location

class InMemTransporter(val local: InMemLocation) extends Transporter {
  override def transport(f: Unit => Bee, destination: Location) {
    InMemTest.getSwarm(destination).receive(f)
  }

  def receive(f: Unit => Bee) {
    InMemTest.currentLocation = Some(local)
    Swarm.continue(f)(this)
  }
}