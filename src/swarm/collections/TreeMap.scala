package swarm.collections

import scala.continuations._ 
import scala.continuations.ControlContext._

import swarm._
import swarm.Swarm._

/*
   INCOMPLETE
*/

class TreeMap[A <% Ordered[A], B]() {
	var left : Option[Ref[TreeMap[A, B]]] = None;
	var right : Option[Ref[TreeMap[A, B]]] = None;
	var kv : Option[(A, B)] = None;

	def apply(k : A) : Option[B] @cps[Bee, Bee] = {
		kv match {
			case Some((key, value)) if k == key => Some(value)
			case Some((key, value)) if k < key => left match {
				case None => None
				case Some(ref) => ref().apply(k)
			}
			case Some((key, value)) if k > key => right match {
				case None => None
				case Some(ref) => ref().apply(k)
			}
			case _ => None // Only remaining possibility is None
		}
	}
	
	def update(k : A, v : B) : Unit @cps[Bee, Bee] = {
		kv match {
			case Some((key, value)) if k == key => kv = Some((k, v))
			case Some((key, value)) if k < key => {
				if (left.isEmpty) {
					left = Some(Ref(new TreeMap[A, B]()));
				}
				left.get().update(k, v);
			}
			case Some((key, value)) if k > key => {
				if (right.isEmpty) {
					right = Some(Ref(new TreeMap[A, B]()));
				}
				right.get().update(k, v);
			}
			case _ => kv = Some((k, v))
		}
	}
}
