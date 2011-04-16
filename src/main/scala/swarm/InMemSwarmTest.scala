package swarm

object InMemSwarmTest {

  import Swarm.swarm

  def main(args: Array[String]) {
    InMemSwarm.getSwarm(InMemLocation(1)).spawn(imst)
  }

  def imst(u: Unit): Bee @swarm = {
    println("one")
    InMemSwarm.getSwarm(InMemLocation(1)).moveTo(InMemLocation(2))
    println("two")
    InMemSwarm.getSwarm(InMemLocation(2)).moveTo(InMemLocation(1))
    println("one again")
    NoBee()
  }
}

object InMemSwarm {
  val swarm1: InMemSwarm = new InMemSwarm(InMemLocation(1))
  val swarm2: InMemSwarm = new InMemSwarm(InMemLocation(2))

  def getSwarm(location: Location): InMemSwarm = location match {
    case InMemLocation(1) => swarm1
    case InMemLocation(2) => swarm2
  }
}

case class InMemLocation(val id: Int) extends Location

class InMemSwarm(val local: InMemLocation) extends SwarmExecutor {
  override def isLocal(location: Location): Boolean = local == location
  override def transmit(f: Unit => Bee, destination: Location) {
    InMemSwarm.getSwarm(destination).receive(f)
  }
  def receive(f: Unit => Bee) {
    continue(f)
  }
}