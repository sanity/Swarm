package swarm

import scala.util.continuations._ 
//import scala.util.continuations.ControlContext._ 
//import scala.util.continuations.Loops._

import java.net._
import java.io._

object Swarm {
	type swarm = cpsParam[Bee, Bee];
	
	var myLocation : Location = null;
	
	var shouldLog = true;
	
	def isLocal(loc : Location) = {
		loc.equals(myLocation);
	}

	def log(message : String) = {
		if (shouldLog) Option(myLocation).map(location => println(location.port + " : " + message))
	}

	def listen(port : Short) = {
		myLocation = new Location(InetAddress.getLocalHost(), port);
	
		val srvr = new ServerSocket(myLocation.port);

		var listenThread = new Thread() {
			override def run() = {
				while (true) {
					log("Waiting for connection");
					val sock = srvr.accept();
					log("Received connection");
					val ois = new ObjectInputStream(sock.getInputStream());
					val bee = ois.readObject().asInstanceOf[(Unit => Bee)];
					log("Executing continuation");
					Swarm.run((Unit) => shiftUnit(bee()));
				}
			}
		}
		listenThread.start();
		Thread.sleep(500);
	}
	
	
	def run(toRun : Unit => Bee @swarm) = {
		execute(reset {
			log("Running task");
			toRun();
//			log("Completed task");
//			NoBee()
		})
	}
	
	/**
	 * Start a new Swarm task (will return immediately as
	 * task is started in a new thread)
	 */
	def spawn(toRun : Unit => Bee @swarm) = {
		val thread = new Thread() {
			override def run() = {
				execute(reset {
					log("Running task");
					toRun();
					log("Completed task");
					NoBee()
				})
			}
		};
		thread.start();
	}
	
	def moveTo(location : Location) = shift {
		c: (Unit => Bee) => {
			log("Move to")
			if (Swarm.isLocal(location)) {
				log("Is local")
				c()
//				NoBee()
			} else {
				log("Moving task to "+location.port);
				IsBee(c, location)
			}
		}
	}
	
	def execute(bee : Bee) = {
		bee match {
			case IsBee(contFunc, location) => {
				log("Transmitting task to "+location.port);
				val skt = new Socket(location.address, location.port);
				val oos = new ObjectOutputStream(skt.getOutputStream());
				oos.writeObject(contFunc);
				oos.close();
				log("Transmission complete");
			}
			case NoBee() => {
				log("No more continuations to execute");
			}
		}
	}
}
