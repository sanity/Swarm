package org.swarmframework.data

/**
 * A Repository instance is used by the Store to store and retrieve data.
 * Potentially different implementations can be used to connect to different types of storage mechanisms.
 */
trait Repository {
  def get[A](uid: Long): Option[A]

  def add[A](value: A): Long

  def remove(uid: Long)

  def exists(uid: Long): Boolean
}

/**
 * A simple implementation of a Repository.
 */
object SimpleRepository extends Repository {
  import java.util.concurrent.ConcurrentHashMap
  import scala.collection.JavaConverters._

  private[this] var nextUid: Long = 0L
  private[this] val store = new ConcurrentHashMap[Long, Any]() asScala

  def get[A](uid: Long): Option[A] = store.get(uid).asInstanceOf[Option[A]]

  def add[A](value: A): Long = {
    synchronized {
      nextUid += 1
      store(nextUid) = value
      nextUid
    }
  }

  def update[A](uid: Long, newValue: A) = store.put(uid, newValue)

  def remove(uid: Long) = store.remove(uid)

  def exists(uid: Long): Boolean = store.contains(uid)
}