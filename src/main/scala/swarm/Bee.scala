package swarm

@serializable sealed trait Bee

case class NoBee() extends Bee

case class IsBee(contFunc: (Unit => Bee), location: Location) extends Bee
