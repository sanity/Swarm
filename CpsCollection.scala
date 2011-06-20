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
    }
  }
}

