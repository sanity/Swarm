package swarm

import swarm.Swarm.swarm

object Store {
  var nextUid = 0

  case class Relocated(val uid: Long, val location: Location)
  val relocated = new collection.mutable.HashMap[Long, Ref[_]]()
  val store = new collection.mutable.HashMap[Long, Any]()

  def apply[A](t: Class[A], uid: Long): Option[A]@swarm = {
    store.get(uid).asInstanceOf[Option[A]]
  }

  def save(value: Any): Long = {
    val uid = nextUid
    nextUid += 1
    store(uid) = value
    return uid
  }

  def relocate(key: Long, newRef: Ref[_]): Unit = {
    store.remove(key)
    relocated.put(key, newRef)
  }
}
