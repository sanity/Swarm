package swarm

import swarm.Swarm.swarm

trait Repository {
  def get[A](uid: Long): Option[A]
  def add[A](value: A): Long
  def remove(uid: Long)
  def exists(uid: Long): Boolean
}

object SimpleRepository extends Repository {
  private[this] var nextUid: Long = 0L
  private[this] val store = new collection.mutable.HashMap[Long, Any]()
  def get[A](uid: Long): Option[A] = store.get(uid).asInstanceOf[Option[A]]
  def add[A](value: A): Long = {
    nextUid += 1
    store(nextUid) = value
    nextUid
  }
  def remove(uid: Long) = store.remove(uid)
  def exists(uid: Long): Boolean = store.contains(uid)
}

object Store {

  private[this] var repository = SimpleRepository

  case class Relocated(val uid: Long, val location: Location)
  val relocated = new collection.mutable.HashMap[Long, Ref[_]]()

  def apply[A](t: Class[A], uid: Long): Option[A]@swarm = {
    repository.get(uid).asInstanceOf[Option[A]]
  }

  def save(value: Any): Long = {
    repository.add(value)
  }

  def relocate(key: Long, newRef: Ref[_]): Unit = {
    repository.remove(key)
    relocated.put(key, newRef)
  }

  def exists(key: Long) = repository.exists(key)
}
