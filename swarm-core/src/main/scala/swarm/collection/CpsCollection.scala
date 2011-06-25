import scala.collection.IterableLike
import scala.collection.GenTraversableOnce
import scala.collection.generic.CanBuildFrom
import scala.util.continuations._

object CpsCollection {
  // implicit coversion from iterable-like collections to cps-friendly collections
  implicit def cpsIterable[A, Repr](xs: IterableLike[A, Repr]) = new {
    def cps = new {
      def foreach[B](f: A => Any@cpsParam[Unit, Unit]): Unit@cpsParam[Unit, Unit] = {
        val it = xs.iterator
        while(it.hasNext) f(it.next)
      }
      def map[B, That](f: A => B@cpsParam[Unit, Unit])(implicit cbf: CanBuildFrom[Repr, B, That]): That@cpsParam[Unit, Unit] = {
        val b = cbf(xs.repr)
        foreach(b += f(_))
        b.result
      }
      def flatMap[B, That](f: A => GenTraversableOnce[B]@cpsParam[Unit, Unit])(implicit cbf: CanBuildFrom[Repr, B, That]): That@cpsParam[Unit, Unit] = {
        val b = cbf(xs.repr)
        for (x <- this) b ++= f(x)
        b.result
      }
      def filter(f: A => Boolean@cpsParam[Unit, Unit])(implicit cbf: CanBuildFrom[Repr, A, Repr]): Repr@cpsParam[Unit, Unit] = {
        val b = cbf(xs.repr)
        for (x <- this)
          if (f(x)) b += x
        b.result
      }
      def foldLeft[B](z: B)(f: (B, A) => B@cpsParam[Unit, Unit]): B@cpsParam[Unit, Unit] = {
        val it = xs.iterator
        var acc: B = z
        while(it.hasNext) acc = f(acc, it.next)
        acc
      }
      def reduceLeft[B >: A](f: (B, A) => B@cpsParam[Unit, Unit]): B@cpsParam[Unit, Unit] = {
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
