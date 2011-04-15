package swarm

/**
 Represents a reference to an object which may reside on a remote computer.
 If apply() is called to retrieve the remote object, it will result in
 the thread being serialized and moved to the remote computer, before
 returning the object.
 **/
@serializable class InetRef[Type](val typeClass : Class[Type], val location : Location, val uid : Long) {

	def apply() = {
		InetSwarm.moveTo(location);
		Store(typeClass, uid) match {
			case Some(v) => v
			case None => throw new RuntimeException("Unable to find item with uid "+uid+" in local store");
		};
	}
}

object InetRef {

  import swarm.InetSwarm.swarm

	def apply[Type](value : AnyRef): InetRef[Type] @swarm = {
		apply(InetSwarm.local, value);
	}
	
	def apply[Type](location : Location, value : AnyRef): InetRef[Type] @swarm = {
		InetSwarm.moveTo(location);
		val uid = Store.save(value);
		new InetRef[Type](value.getClass().asInstanceOf[Class[Type]], location, uid);
	}
	
	def unapply[Type](ref : InetRef[Type]) = {
		Some(ref())
	}
}
