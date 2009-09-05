package swarm.demos;

import swarm._;
import swarm.Swarm._;

object ExplicitMoveTo1 {
	def main(args : Array[String]) = {
		if (args.length > 1 && args(1) == "start") {
			Swarm.spawn(emt1Thread);
		}

		Swarm.listen(java.lang.Short.parseShort(args(0)));
	}
	
	def emt1Thread(u : Unit) = {
		val name = scala.Console.readLine("What is your name? : ");
		moveTo(new Location(myLocation.address, 9997))
		val age = scala.Console.readLine("Hello "+name+", what age are you? : ")
		moveTo(new Location(myLocation.address, 9998))
		println("And back again "+name+" who is "+age+" years old")
		NoBee()
	}
}