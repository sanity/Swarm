package swarm

import scala.continuations._ 
import scala.continuations.ControlContext._ 
import scala.continuations.Loops._

import java.net._
import java.io._

object Swarm {
	var myLocation : Location = null;
	
	def isLocal(loc : Location) = {
		loc.equals(myLocation);
	}

	def main(args : List[String]) {
		myLocation = new Location(InetAddress.getLocalHost(), java.lang.Short.parseShort(args(0)));
	
		val srvr = new ServerSocket(myLocation.port);
		
		while (true) {
			val sock = srvr.accept();
			val ois = new ObjectInputStream(sock.getInputStream());
			val bee = ois.readObject().asInstanceOf[(Unit => Bee)];
			run(bee);
		}
	}
	
	/**
	 * Start a new Swarm task (will return immediately as
	 * task is started in a new thread)
	 */
	def run(toRun : Unit => Bee @cps[Bee, Bee]) = {
		val thread = new Thread() {
			override def run() = {
				reset {
					toRun();
					NoBee()
				}
			}
		};
		thread.start();
	}
	
	def moveTo(location : Location) = shift { 
		c: (Unit => Bee) => {
			if (Swarm.isLocal(location)) {
				c(location)
				NoBee()
			} else {
				IsBee(c, location)
			}
		}
	}
	
	def execute(bee : Bee) = {
		bee match {
			case IsBee(contFunc, location) => {
				val skt = new Socket(location.address, location.port);
				val oos = new ObjectOutputStream(skt.getOutputStream());
				oos.writeObject(contFunc);
				oos.close();
			}
			case NoBee() => {
				println("No more continuations to execute");
			}
		}
	}
}
