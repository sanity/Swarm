package swarm.data

import swarm.Swarm.swarm
import swarm.transport.Location
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

/**
 * The Store is the single point of data storage per Swarm node.
 * A Repository implementation is used to manage the stored data.
 */
object Store {

  private[this] val repository = SimpleRepository

  case class Relocated(uid: Long, location: Location)

  val relocated = new ConcurrentHashMap[Long, Ref[_]]() asScala

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
