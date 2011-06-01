package swarm.data

import swarm.Swarm.swarm
import swarm.Swarm
import swarm.transport.{Transporter, Location}

/**
 * RefMap represents a map of Ref instances.
 */
class RefMap[A](typeClass: Class[A], refMapKey: String) extends Serializable {

  // TODO this should really be a map of [String, Ref], however this causes client-side ClassNotFound exceptions when trying to load the Ref class after Swarm.moveTo() in the put method below
  private[this] val map = new collection.mutable.HashMap[String, Tuple3[Class[A], Location, Long]]()

  /**
   * Dereference and return the value (if any) referenced by the given key.
   */
  def get(key: String)(implicit tx: Transporter, local: Location): Option[A] =
    map.get(key).map(tuple => Swarm.spawnAndReturn((new Ref(tuple._1, tuple._2, tuple._3))()))

  /**
   * Add the given data to the local map.
   * Create a new Ref instance in each node within the Swarm cluster to reference the single instance of the stored data.
   */
  def put(location: Location, key: String, value: A)(implicit m: scala.reflect.Manifest[A], tx: Transporter, local: Location): Unit = {
    val tuple = map.getOrElse(key, Swarm.spawnAndReturn{ val ref = Ref(location, value); (ref.typeClass, ref.location, ref.uid) })
    val ref = new Ref(tuple._1, tuple._2, tuple._3)
    Swarm.spawn{ ref.update(value); RefMap.update(refMapKey, key, (ref.typeClass, ref.location, ref.uid)) }
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

  private[this] var _locations: List[Location] = _

  /**
   * Crudely specify the locations in the Swarm cluster.
   */
  def locations_=(locations: List[Location]) {
    _locations = locations
  }

  def locations = _locations

  def get[A](typeClass: Class[A], key: String)(implicit tx: Transporter, local: Location): RefMap[A] = Swarm.spawnAndReturn(RefMap(typeClass, key))

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
    updateForLocations(locations, refMapKey, key, tuple)
  }

  /**
   * A recursive method to iterate over all locations and find and update a Ref.  This is needed because a for comprehension doesn't play well with CPS code.
   */
  private def updateForLocations[A](locations: List[Location], refMapKey: String, key: String, tuple: Tuple3[Class[A], Location, Long])(implicit m: scala.reflect.Manifest[A]): Unit@swarm = {
    locations match {
      case Nil =>
      case location :: moreLocations =>
        Swarm.moveTo(location)
        RefMap(tuple._1, refMapKey).put(key, tuple)
        updateForLocations(moreLocations, refMapKey, key, tuple)
    }
  }
}
