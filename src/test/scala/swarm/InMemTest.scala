package swarm

import Swarm.swarm
import org.scalatest.FunSuite
import util.continuations._

class InMemTest extends FunSuite {

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

      val sRef1 = Ref(InMemLocation(1), "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      val sRef2 = Ref(InMemLocation(2), "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      assert(sRef1() === "test string one")
      assert(InMemTest.currentLocation === Some(InMemLocation(1)))

      assert(sRef2() === "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      NoBee()
    }
  }
}

object InMemTest {
  val swarm1: InMemTransporter = new InMemTransporter(InMemLocation(1))
  val swarm2: InMemTransporter = new InMemTransporter(InMemLocation(2))

  def getSwarm(location: Location): InMemTransporter = location match {
    case InMemLocation(1) => swarm1
    case InMemLocation(2) => swarm2
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