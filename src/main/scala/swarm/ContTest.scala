package swarm;

import scala.util.continuations._ 
//import scala.continuations.ControlContext._ 
//import scala.continuations.Loops._
//import scala.util.continuations.ControlContext.{shift,reset}

object ContTest {

	def get(k : String) = shift { 
		c: (String => Cont) => {
			if (k == "local") {
				c("localValue")
				NoCont()
			} else {
				IsCont(c, "remote location")
			}
		}
	}
	
	def root1() : Cont = reset {
		println("Attempting first get");
		println("First get result: "+get("remote"));
		println("Second get result: "+get("remote"));
		NoCont()
	}
	/*  How does one implement this now that ControlContext is a class?
	def root2() : Cont = reset {
		val num = List(1,2,3,4,5);
		for (n <- num.suspendable) {
			println("get "+get("remote"));
		}
		NoCont()
	}*/

	def main(args: Array[String]) {
		execute(root1())
		// execute(root2())
	}
	
	def execute(cont : Cont) {
		println("Execute("+cont+")")
		cont match {
			case IsCont(contFunc, location) => {
				println("Continuation moved to "+location+", executing")
				execute(contFunc("remote value"));
			}
			case NoCont() => {
				println("No more continuations to execute")
			}
		}
	}
        
}

abstract class Cont
case class NoCont() extends Cont
case class IsCont(contFunc : (String => Cont), location : String) extends Cont
