package org.swarmframework.internal

import org.swarmframework.transport._
import org.swarmframework.data._

/**
 * The Bee trait sits at the boundaries of the cps type declarations in Swarm,
 * and represents the continuation yet to execute.  Different implementations
 * can be used to delimit the end of a Swarm-enabled function, mark a
 * continuation for transfer to another location, or hold a (potentially
 * remote) reference to data.
 */
sealed trait Bee extends Serializable

case class NoBee() extends Bee

case class IsBee(contFunc: (Unit => Bee), location: Location) extends Bee

case class RefBee(contFunc: (Unit => Bee), ref: Ref[_]) extends Bee
