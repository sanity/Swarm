package swarm.demos;

import swarm._;
import swarm.Swarm._;
import scala.Console._;
import Integer._;

object ExplicitMoveTo1 {
	def main(args : Array[String]) = {		
		Swarm.listen(java.lang.Short.parseShort(args(0)));

		if (args.length > 1 && args(1) == "start") {
			Swarm.spawn(emt1Thread);
		}

		while(true) {
			Thread.sleep(1000);
		}
	}
	
	def emt1Thread(u : Unit) = {
		val name = scala.Console.readLine("What is your name? : ");
		moveTo(new Location(myLocation.address, 9997))
		val age = parseInt(readLine("Hello "+name+", what age are you? : "))
		moveTo(new Location(myLocation.address, 9998))
		println("Wow "+name+", you're half way to "+(age*2)+" years old")
		NoBee()
	}
}