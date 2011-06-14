package swarm.collection

import swarm.Swarm._
import swarm.Swarm
import swarm.transport.{Transporter, Location}
import java.util.UUID
import swarm.data.Ref

/**
 * RefMap represents a map of Ref instances.
 */
class RefMap[A](typeClass: Class[A], refMapKey: String) extends Serializable {

  // TODO this should really be a map of [String, Ref], however this causes client-side ClassNotFound exceptions when trying to load the Ref class after Swarm.moveTo() in the put method below
  private[this] val map = new collection.mutable.HashMap[String, Tuple3[Class[A], Location, Long]]()

  /**
   * Dereference and return the value (if any) referenced by the given key.
   */
  def get(key: String): Option[A]@swarm = {
    if (map.contains(key)) {
      val tuple = map(key)
      val ref = new Ref(tuple._1, tuple._2, tuple._3)
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
      val tuple = map(key)
      val ref = new Ref(tuple._1, tuple._2, tuple._3)
      ref.update(value)
    } else {
      // The mapStore does not know about this id, so assume that no nodes have a reference to this value in their stores; create a Ref and add it to every Swarm store
      val ref = Ref(location, value)
      RefMap.update(refMapKey, key, (ref.typeClass, ref.location, ref.uid))
    }
  }

  protected def put(key: String, tuple: Tuple3[Class[A], Location, Long]) {
    map(key) = tuple
  }
}

/**
 * RefMap is a type constructor which and generates RefMap instances.
 */
object RefMap {

  private[this] val map = new collection.mutable.HashMap[String, RefMap[_]]()

  private[this] var _locations: List[Location] = _ // TODO expunge this var

  /**
   * Crudely specify the locations in the Swarm cluster.
   */
  def locations_=(locations: List[Location]) {
    _locations = locations
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
  def update[A](refMapKey: String, key: String, tuple: Tuple3[Class[A], Location, Long])(implicit m: scala.reflect.Manifest[A]): Unit@swarm = {
    Swarm.foreach(locations, {
      location: Location =>
        Swarm.moveTo(location)
        RefMap(tuple._1, refMapKey).put(key, tuple)
    })
  }
}
