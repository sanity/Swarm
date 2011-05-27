package swarm.data

import swarm.Swarm.swarm
import scala.Predef._
import swarm.{Logs, Swarm}
import swarm.transport.{Transporter, Location}

/**
 * Ref represents a reference to an object which may reside on a remote computer.
 * If apply() is called to retrieve the remote object, it will result in the thread being
 * serialized and moved to the remote computer before returning the object.
 */
class Ref[A](val typeClass: Class[A], val initLoc: Location, val initUid: Long) extends Serializable with Logs {

  private[this] var _location = initLoc
  private[this] var _uid = initUid

  def location: Location = _location

  def uid: Long = _uid

  /**
   * Called when the data referenced by this Ref has been moved.
   * Subsequent calls to apply() will result in relocation to the updated location.
   */
  def relocate(newUid: Long, newLocation: Location) {
    _uid = newUid
    _location = newLocation
  }

  /**
   * Dereference and return the data referenced by this Ref.
   */
  def apply(): A@swarm = {
    debug("dereferencing Ref " + uid + "@" + location)
    Swarm.dereference(this)
    Store(typeClass, uid).getOrElse(throw new RuntimeException("Unable to find item with uid " + uid + " in local store"))
  }

  /**
   * Update the data value referenced by this Ref.
   */
  def update(newValue: A): Unit@swarm = {
    Swarm.dereference(this)
    Store.update(uid, newValue)
  }
}

/**
 * Ref is a type constructor which adds data to the local Store and generates a Ref instance of the data's type.
 */
object Ref {

  /**
   * Store the given value in the local Store, then generate a new Ref instance with the given location and value.
   */
  def apply[A](location: Location, value: A)(implicit m: scala.reflect.Manifest[A]): Ref[A]@swarm = {
    Swarm.moveTo(location)
    val uid = Store.save(value)
    new Ref[A](m.erasure.asInstanceOf[Class[A]], location, uid);
  }

  def unapply[A](ref: Ref[A]) = {
    Some(ref())
  }
}

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
  def put(location: Location, key: String, value: A)(implicit m: scala.reflect.Manifest[A], tx: Transporter, local: Location): Unit = Swarm.spawn {
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

  private[this] var _locations: List[Location] = _

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
