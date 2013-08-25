package swarm.experiments.clustering

/*
 * A set of traits that various clustering algorithms can work with
 */

import scala.collection._

/**
 * An immutable class that represents an arrangement of Datums between computers.
 */
case class Configuration private(datumsByComputer: immutable.Map[Computer, immutable.Set[Datum]],
                                 computersByDatum: immutable.Map[Datum, Computer]) {

  /**
   *  Efficiently produces a new configuration where a single datum has been
   *  moved from one computer to another. This does not modify the current
   *  Configuration.
   *
   * @param datum The datum to move
   * @param destinationComputer The computer to move it to
   * @return The updated Configuration
   */
  def moveTo(datum: Datum, destinationComputer: Computer): Configuration = {
    computersByDatum.get(datum) match {
      case Some(sourceComputer) => {
        val origSourceDatumSet = datumsByComputer.get(sourceComputer) match {
          case Some(o) => o
          case _ => throw new RuntimeException("Can't find source computer")
        }
        val origDestinationDatumSet : immutable.Set[Datum] = datumsByComputer.getOrElse(destinationComputer, immutable.Set[Datum]())
        val newDatumsByComputer = ((datumsByComputer
          + (sourceComputer -> (origSourceDatumSet - datum)))
          + (destinationComputer -> (origDestinationDatumSet + datum))
          )
        val newComputersByDatum = computersByDatum + (datum -> destinationComputer)
        return new Configuration(newDatumsByComputer, newComputersByDatum)
      }
      case None => throw new IllegalArgumentException("Unknown Datum")
    }
  }
}

object Configuration {
  def apply(datumsByComputer: immutable.Map[Computer, scala.collection.immutable.Set[Datum]]): Configuration = {
    val mutableComputerByDatum = new mutable.HashMap[Datum, Computer]()
    for ((computer, datums) <- datumsByComputer) {
      for (datum <- datums) {
        mutableComputerByDatum.put(datum, computer)
      }
    }
    return new Configuration(datumsByComputer, mutableComputerByDatum.toMap)
  }
}

/**
 * A computer that contains datums
 */
trait Computer {
  def datums: Set[Datum]
}

/**
 * A connection between two Datums, which contains information about
 * how frequently they are accessed one immediately after the other
 */
trait Edge {
  def derefsPerSecond: Double
}

/**
 * A piece of data
 */
trait Datum {
  def edgesByDatum: Map[Datum, Edge]

  def remoteDPS: Number = {
    (for ((d, e) <- edgesByDatum if d.computer != this.computer) yield e.derefsPerSecond).sum
  }

  def computer: Computer
}