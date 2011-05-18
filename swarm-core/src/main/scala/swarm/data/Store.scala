package swarm.data

import swarm.Swarm.swarm
import swarm.transport.Location

/**
 * The Store is the single point of data storage per Swarm node.
 * A Repository implementation is used to manage the stored data.
 */
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

  def update[A](key: Long, newValue: A): Unit = {
    repository.update(key, newValue)
  }

  def exists(key: Long) = repository.exists(key)
}
