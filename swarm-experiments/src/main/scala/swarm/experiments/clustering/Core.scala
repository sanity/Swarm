package swarm.experiments.clustering

/*
 * A set of traits that various clustering algorithms can work with
 */

import scala.collection._

class RRPSScore(configuration : Configuration, dereferenceTracker : DereferenceTracker) {
  def getScore() : Double = {
    throw new RuntimeException()
  }

  def updateCon
}

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
}

/**
 * This class is responsible for keeping track of dereferences, which it does
 * by storing the edges and notifying the appropriate edge when a dereference
 * occurs.  It can support multiple edge types, and must be passed a factory
 * for the appropriate edge type on construction.
 *
 * @param edgeFactory
 * @tparam E
 */
class DereferenceTracker[E <: Edge](edgeFactory : {def newEdge() : E}) {
  val edges = mutable.Map[(Datum, Datum), E]()
  val edgesByFrom = mutable.Map[Datum, Set[E]]()
  val edgesByTo = mutable.Map[Datum, Set[E]]()

  def recordDereference(from : Datum, to : Datum) {
    val edge = edges.get((from, to)) match {
      case Some(edge) => edge
      case None => {
        val newEdge = edgeFactory.newEdge()
        edges.put((from, to), newEdge)
        edgesByFrom.getOrElseUpdate(from, mutable.Set()) += newEdge
        edgesByTo.getOrElseUpdate(to, mutable.Set()) += newEdge
        newEdge
      }
    }
    edge.notifyOfDeref()
  }
}

/**
 * A connection between two Datums, which contains information about
 * how frequently they are accessed one immediately after the other
 */
trait Edge[DerefInfo] {
  val from : Datum
  val to : Datum

  def notifyOfDeref()

  def derefsPerSecond: Double
}

/**
 * A piece of data
 */
trait Datum {
}