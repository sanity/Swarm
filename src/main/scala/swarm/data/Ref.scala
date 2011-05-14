package swarm.data

import swarm.Swarm
import swarm.Swarm.swarm
import swarm.transport.Location

/**
Represents a reference to an object which may reside on a remote computer.
 If apply() is called to retrieve the remote object, it will result in
 the thread being serialized and moved to the remote computer, before
 returning the object.
 **/
class Ref[A](val typeClass: Class[A], val initLoc: Location, val initUid: Long) extends Serializable {

  private[this] var _location = initLoc
  private[this] var _uid = initUid

  def location: Location = _location

  def uid: Long = _uid

  def relocate(newUid: Long, newLocation: Location) {
    _uid = newUid
    _location = newLocation
  }

  def apply(): A@swarm = {
    Swarm.dereference(this)
    Store(typeClass, uid).getOrElse(throw new RuntimeException("Unable to find item with uid " + uid + " in local store"))
  }

  def update(newValue: A): Unit@swarm = {
    Swarm.dereference(this)
    Store.update(uid, newValue)
  }
}

object Ref {

  import swarm.Swarm.swarm

  // TODO "broadcast" the new Ref by creating a corresponding Ref (holding this location) in all other Swarm locations
  def apply[A](location: Location, value: A)(implicit m: scala.reflect.Manifest[A]): Ref[A]@swarm = {
    Swarm.moveTo(location)
    val uid = Store.save(value)
    new Ref[A](m.erasure.asInstanceOf[Class[A]], location, uid);
  }

  def unapply[A](ref: Ref[A]) = {
    Some(ref())
  }
}

object RefMap {

  private[this] var _locations: List[Location] = _

  def locations(locations: List[Location]) {
    _locations = locations
  }

  private[this] var _local: Location = _

  def local(location: Location) = _local = location

  // TODO make mapStore more generic, to hold arbitrary values rather than List[String].  This might call for a RefMap type constructor
  val mapStore = new collection.mutable.HashMap[String, Ref[String]]()

  def put(mapId: String, newValue: String): Unit@swarm = {
    if (mapStore.contains(mapId)) {
      // The mapStore knows about this id, so assume that all nodes have a reference to this value in their stores
      val ref: Ref[String] = mapStore(mapId)
      ref.update(newValue)
    } else {
      // The mapStore does not know about this id, so assume that no nodes have a reference to this value in their stores; create a Ref and add it to every Swarm store
      val ref: Ref[String] = Ref(_local, newValue)
      mapStore(mapId) = ref

      // TODO for each location, add ref to the local Store
    }
  }

  def get[A](clazz: Class[A], id: String): Option[A]@swarm = {
    if (mapStore.contains(id)) {
      Some((mapStore(id).asInstanceOf[Ref[A]])())
    } else {
      None
    }
  }
}
