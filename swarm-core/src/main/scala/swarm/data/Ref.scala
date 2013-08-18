package org.swarmframework.data

import org.swarmframework.core.Swarm._
import org.swarmframework.internal._
import org.swarmframework.transport.Location

/**
 * Ref represents a reference to an object which may reside on a remote computer.
 * If apply() is called to retrieve the remote object, it will result in the thread being
 * serialized and moved to the remote computer before returning the object.
 */
class Ref[A](val typeClass: Class[A], initLoc: Location, initUid: Long) extends Serializable with Logs {

  private[this] var _location = initLoc
  private[this] var _uid = initUid

  def location = _location

  def uid = _uid

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
    debug(s"dereferencing Ref $uid @ $location")
    dereference(this)
    Store(typeClass, uid).getOrElse(throw new RuntimeException(s"Unable to find item with uid $uid in local store"))
  }

  /**
   * Update the data value referenced by this Ref.
   */
  def update(newValue: A): Unit@swarm = {
    dereference(this)
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
    moveTo(location)
    val uid = Store.save(value)
    new Ref[A](m.runtimeClass.asInstanceOf[Class[A]], location, uid)
  }

  def unapply[A](ref: Ref[A]) = {
    Some(ref())
  }
}
