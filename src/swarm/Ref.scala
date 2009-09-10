package swarm

import swarm.Swarm._

import scala.continuations._ 
import scala.continuations.ControlContext._ 
import scala.actors.remote._

@serializable class Ref[Type](val typeClass : Class[Type], val location : Location, val uid : Long) {
	def apply() = {
		Swarm.moveTo(location);
		Store(typeClass, uid) match {
			case Some(v) => v
			case None => throw new RuntimeException("Unable to find item with uid "+uid+" in local store");
		};
	} : Type @swarm
}

object Ref {
	def apply[Type](value : AnyRef) : Ref[Type] @cps[Bee, Bee] = {
		apply(Swarm.myLocation, value);
	}
	
	def apply[Type](location : Location, value : AnyRef) : Ref[Type] @cps[Bee, Bee] = {
		Swarm.moveTo(location);
		val uid = Store.save(value);
		new Ref[Type](value.getClass().asInstanceOf[Class[Type]], location, uid);
	}
}