package swarm

@serializable sealed trait Bee

case class NoBee() extends Bee

case class IsBee(contFunc: (Unit => Bee), location: Location) extends Bee

case class RefBee(contFunc: (Unit => Bee), ref: Ref[_]) extends Bee
