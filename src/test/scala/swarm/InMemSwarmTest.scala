package swarm

import org.scalatest.FunSuite

class InMemSwarmTest extends FunSuite {

  test("execution moves from one swarm to another") {
    import Swarm.swarm

    Swarm.spawn(imst)(InMemSwarm.getSwarm(InMemLocation(1)))

    def imst(u: Unit): Bee@swarm = {
      assert(InMemSwarm.currentLocation == null)

      Swarm.moveTo(InMemLocation(2))
      assert(InMemSwarm.currentLocation == InMemLocation(2))

      Swarm.moveTo(InMemLocation(1))
      assert(InMemSwarm.currentLocation == InMemLocation(1))

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
    Swarm.continue(f)(this)
  }
}