package swarm

/**
 * The Bee trait sits at the boundaries of the cps type declarations in Swarm, 
 * and represents the continuation yet to execute.  Different implementations 
 * can be used to delimit the end of a Swarm-enabled function, mark a 
 * continuation for transfer to another location, or hold a (potentially 
 * remote) reference to data.
 */
sealed trait Bee extends Serializable

case class NoBee() extends Bee

case class IsBee(contFunc: (Unit => Bee), location: swarm.transport.Location) extends Bee

case class RefBee(contFunc: (Unit => Bee), ref: swarm.data.Ref[_]) extends Bee
