package swarm

import swarm.Swarm.swarm

object Store {
  var nextUid = 0

  case class Relocated(val uid: Long, val location: Location)
  val relocated = new collection.mutable.HashMap[Long, Relocated]()
  val store = new collection.mutable.HashMap[Long, Any]()

  def apply[A](t: Class[A], uid: Long): Option[A]@swarm = {
    store.get(uid) match {
      case Some(x) => x match {
        case ref: Ref[A] => Some(ref())
        case a: A => Some(a)
      }
      case None => None
    }
  }

  def save(value: Any): Long = {
    val uid = nextUid
    nextUid += 1
    update(uid, value)
    return uid
  }

  def update(key: Long, value: Any): Unit = {
    store.put(key, value)
  }
}
