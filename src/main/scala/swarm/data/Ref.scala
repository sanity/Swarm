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
@serializable class Ref[A](val typeClass: Class[A], val initLoc: Location, val initUid: Long) {

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
}

object Ref {

  import swarm.Swarm.swarm

  def apply[A](location: Location, value: A)(implicit m: scala.reflect.Manifest[A]): Ref[A]@swarm = {
    Swarm.moveTo(location)
    val uid = Store.save(value)
    new Ref[A](m.erasure.asInstanceOf[Class[A]], location, uid);
  }

  def unapply[A](ref: Ref[A]) = {
    Some(ref())
  }
}
