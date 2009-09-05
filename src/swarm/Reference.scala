package swarm

import scala.continuations._ 
import scala.continuations.ControlContext._ 
import scala.actors.remote._

@serializable class Reference[Type](val typeClass : Class[Type], val location : Location, val uid : Long) {
	def apply() = {
		Swarm.moveTo(location);
		Store(typeClass, uid);
	}
}

object Reference {
	def create[Type](location : Location, value : AnyRef) : Reference[Type] @cps[Bee, Bee] = {
		Swarm.moveTo(location);
		val uid = Store.save(value);
		new Reference[Type](value.getClass().asInstanceOf[Class[Type]], location, uid);
	}
}