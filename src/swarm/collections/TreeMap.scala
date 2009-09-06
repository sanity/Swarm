package swarm.collections

import scala.continuations._ 
import scala.continuations.ControlContext._

import swarm._
import swarm.Swarm._

/*
   INCOMPLETE
*/

class TreeMap[A <% Ordered[A], B](var key : A, var value : B) {
	var left : Option[Ref[TreeMap[A, B]]] = None;
	var right : Option[Ref[TreeMap[A, B]]] = None;

	def get(k : A) : Option[B] @cps[Bee, Bee] = {
		if (k == key) {
			return Some(value);
		} else {
			if (k > key) {
				left match {
					case None => None
					case Some(ref : Ref[TreeMap[A, B]]) => ref().get(k)
				}
			} else {
				right match {
					case None => None
					case Some(ref : Ref[TreeMap[A, B]]) => ref().get(k)
				}
			} 
		}
	}
}
