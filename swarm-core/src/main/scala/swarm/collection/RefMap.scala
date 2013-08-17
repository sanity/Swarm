package swarm.collection

import swarm.Swarm._
import swarm.Swarm
import swarm.transport.Location
import swarm.data.Ref
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

/**
 * RefMap represents a map of Ref instances.
 */
class RefMap[A](typeClass: Class[A], refMapKey: String) extends Serializable {

  // NOTE: this should really be a map of [String, Ref], however this causes client-side ClassNotFound exceptions when trying to load the Ref class after Swarm.moveTo() in the put method below
  // UPDATE: This appears to no longer be an issue, but I'm keeping the above note just in case
  private[this] val map = new ConcurrentHashMap[String, Ref[A]]() asScala

  /**
   * Dereference and return the value (if any) referenced by the given key.
   */
  def get(key: String): Option[A]@swarm = {
    if (map.contains(key)) {
      val ref = map(key)
      Some(ref())
    } else {
      None
    }
  }

  /**
   * Add the given data to the local map.
   * Create a new Ref instance in each node within the Swarm cluster to reference the single instance of the stored data.
   */
  def put(location: Location, key: String, value: A)(implicit m: scala.reflect.Manifest[A]): Unit@swarm = {
    if (map.contains(key)) {
      // The mapStore knows about this id, so assume that all nodes have a reference to this value in their stores
      val ref = map(key)
      ref.update(value)
    } else {
      // The mapStore does not know about this id, so assume that no nodes have a reference to this value in their stores; create a Ref and add it to every Swarm store
      val ref = Ref(location, value)
      RefMap.update(refMapKey, key, ref)
    }
  }

  protected def put(key: String, ref: Ref[A]) {
    map(key) = ref
  }
}

/**
 * RefMap is a type constructor which and generates RefMap instances.
 */
object RefMap {

  private[this] val map = new ConcurrentHashMap[String, RefMap[_]]() asScala

  private[this] var _locations: List[Location] = Nil // TODO expunge this var

  /**
   * Crudely specify the locations in the Swarm cluster.
   */
  def add(location: Location) {
    _locations.synchronized {
      _locations = location :: _locations
    }
  }

  def locations = _locations

  /**
   * Generate a RefMap instance of the given type and key.
   */
  def apply[A](typeClass: Class[A], key: String): RefMap[A]@swarm = {
    if (!map.contains(key)) {
      val refMap: RefMap[A] = new RefMap(typeClass, key)
      map(key) = refMap
    }
    map(key).asInstanceOf[RefMap[A]]
  }

  /**
   * For each location, find the RefMap identified by refMapKey and update the value identified by key to a new Ref created from tuple
   */
  def update[A](refMapKey: String, key: String, ref: Ref[A])(implicit m: scala.reflect.Manifest[A]): Unit@swarm = {
    Swarm.foreach(locations, {
      location: Location =>
        Swarm.moveTo(location)
        RefMap(ref.typeClass, refMapKey).put(key, ref)
    })
  }
}
