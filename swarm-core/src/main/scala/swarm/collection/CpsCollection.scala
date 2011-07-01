package swarm.collection

import scala.collection.IterableLike
import scala.collection.GenTraversableOnce
import scala.collection.generic.CanBuildFrom
import scala.util.continuations._
import swarm.Bee

// TODO these aren't quite working yet due to unserializability
/**
 * An implicit conversion for any IterableLike structure to be usable in 
 * conjunction with continuations.  For the most part, the implementations 
 * below are pulled right from the Scala core code, thoughertain tweaks have 
 * been made to keep things working with the cps type system. 
 */
object CpsCollection {
  // implicit coversion from iterable-like collections to cps-friendly collections
  implicit def cpsIterable[A, Repr](xs: IterableLike[A, Repr]) = new {
    def cps = new {
      def foreach[B](f: A => Any@cpsParam[Bee, Bee]): Unit@cpsParam[Bee, Bee] = {
        val it = xs.iterator
        while(it.hasNext) f(it.next)
      }
      def map[B, That](f: A => B@cpsParam[Bee, Bee])(implicit cbf: CanBuildFrom[Repr, B, That]): That@cpsParam[Bee, Bee] = {
        val b = cbf(xs.repr)
        foreach(b += f(_))
        b.result
      }
      def flatMap[B, That](f: A => GenTraversableOnce[B]@cpsParam[Bee, Bee])(implicit cbf: CanBuildFrom[Repr, B, That]): That@cpsParam[Bee, Bee] = {
        val b = cbf(xs.repr)
        for (x <- this) b ++= f(x)
        b.result
      }
      def filter(f: A => Boolean@cpsParam[Bee, Bee])(implicit cbf: CanBuildFrom[Repr, A, Repr]): Repr@cpsParam[Bee, Bee] = {
        val b = cbf(xs.repr)
        for (x <- this)
          if (f(x)) b += x
        b.result
      }
      def foldLeft[B](z: B)(f: (B, A) => B@cpsParam[Bee, Bee]): B@cpsParam[Bee, Bee] = {
        val it = xs.iterator
        var acc: B = z
        while(it.hasNext) acc = f(acc, it.next)
        acc
      }
      def reduceLeft[B >: A](f: (B, A) => B@cpsParam[Bee, Bee]): B@cpsParam[Bee, Bee] = {
        if (xs.isEmpty)
          throw new UnsupportedOperationException("empty.reduceLeft")

        val it = xs.iterator
        var acc: B = it.next
        while(it.hasNext) acc = f(acc, it.next)
        acc
      }
    }
  }
}
