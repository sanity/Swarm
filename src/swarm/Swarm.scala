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

	def log(message : String) = {
		println(myLocation.port+" : "+message);
	}
	
	def exp1(u : Unit) = {
		val name = scala.Console.readLine("What is your name? : ");
		moveTo(new Location(myLocation.address, 9997))
		val age = scala.Console.readLine("Hello "+name+", what age are you? : ")
		moveTo(new Location(myLocation.address, 9998))
		println("And back again "+name+" who is "+age+" years old")
		NoBee()
	}
	
	def exp2(u : Unit) = {
		val vLoc = Reference.create[String](myLocation, "test local string");
		val vRem = Reference.create[String](new Location(myLocation.address, 9997), "test remote string");
		moveTo(vLoc.location)
		println("moved 1");
		//println(vLoc());
		moveTo(vRem.location)
		println("moved 2");
		//println(vRem());
		NoBee()
	}
	
	def listen(port : Short) = {
		myLocation = new Location(InetAddress.getLocalHost(), port);
	
		val srvr = new ServerSocket(myLocation.port);

		while (true) {
			log("Waiting for connection");
			val sock = srvr.accept();
			log("Received connection");
			val ois = new ObjectInputStream(sock.getInputStream());
			val bee = ois.readObject().asInstanceOf[(Unit => Bee)];
			log("Executing continuation");
			run((Unit) => shiftUnit(bee()));
		}
	}
	
	
	def run(toRun : Unit => Bee @cps[Bee, Bee]) = {
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
	def spawn(toRun : Unit => Bee @cps[Bee, Bee]) = {
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
				println("No more continuations to execute");
			}
		}
	}
}
