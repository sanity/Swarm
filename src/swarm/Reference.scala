package swarm

import scala.continuations._ 
import scala.continuations.ControlContext._ 
import scala.actors.remote._

class Reference[Type](typeClass : Class[Type], location : Location, uid : Long) {
	def apply() = {
		Swarm.moveTo(location);
		Store(typeClass, uid);
	}
}

object Reference {
	def create[Type](location : Location, value : AnyRef) = {
		Swarm.moveTo(location);
		val uid = Store.save(value);
		new Reference[Type](value.getClass().asInstanceOf[Class[Type]], location, uid);
	}
}