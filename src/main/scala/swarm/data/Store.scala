package swarm.data

import swarm.Swarm.swarm
import swarm.transport.Location

// TODO make this contention safe
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
