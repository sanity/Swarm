package swarm

sealed trait Bee extends Serializable

case class NoBee() extends Bee

case class IsBee(contFunc: (Unit => Bee), location: swarm.transport.Location) extends Bee

case class RefBee(contFunc: (Unit => Bee), ref: swarm.data.Ref[_]) extends Bee
