package swarm.demos

import swarm._
import org.scalatest.FunSuite

class InMemRefTest extends FunSuite {

  test("execution moves from one swarm to another") {
    import Swarm.swarm

    Swarm.spawn(imrt)(InMemSwarm.getSwarm(InMemLocation(1)))

    def imrt(u: Unit): Bee@swarm = {
      val sRef2 = Ref(InMemLocation(2), "test string two")
      val sRef1 = Ref(InMemLocation(1), "test string one")

      assert(sRef1.uid == 1)
      assert(sRef1() == "test string one")

      assert(sRef2.uid == 2)
      assert(sRef2() == "test string two")

      NoBee()
    }
  }
}

object InMemSwarm {
  val swarm1: InMemTransporter = new InMemTransporter(InMemLocation(1))
  val swarm2: InMemTransporter = new InMemTransporter(InMemLocation(2))

  def getSwarm(location: Location): InMemTransporter = location match {
    case InMemLocation(1) => swarm1
    case InMemLocation(2) => swarm2
  }

  var currentLocation: InMemLocation = _
}

case class InMemLocation(val id: Int) extends Location

class InMemTransporter(val local: InMemLocation) extends Transporter {
  override def transport(f: Unit => Bee, destination: Location) {
    InMemSwarm.getSwarm(destination).receive(f)
  }

  def receive(f: Unit => Bee) {
    InMemSwarm.currentLocation = local
    Store.nextUid = local.id
    Swarm.continue(f)(this)
  }
}