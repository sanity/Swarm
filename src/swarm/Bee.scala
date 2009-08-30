package swarm

@serializable abstract class Bee
@serializable case class NoBee() extends Bee
@serializable case class IsBee(contFunc : (() => Bee), location : Location) extends Bee
