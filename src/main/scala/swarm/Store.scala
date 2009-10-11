package swarm

import scala.collection.mutable._

object Store {
	var nextUid = 0;
	
	val map = new HashMap[Long, Any]();
	
	def apply[T](t : Class[T], key : Long) : Option[T] = {
		map.get(key).asInstanceOf[Option[T]];
	}
	
	def save(value : Any) : Long = {
		val uid = nextUid;
		nextUid+=1;
		update(uid, value);
		return uid;
	}
	
	def update(key : Long, value : Any) : Unit = {
		map.put(key, value);
	}
}
