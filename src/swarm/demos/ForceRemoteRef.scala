package swarm.demos;

import swarm._;
import swarm.Swarm._;

object ForceRemoteRef {
	def main(args : Array[String]) = {
		Swarm.listen(java.lang.Short.parseShort(args(0)));
		if (args.length > 1 && args(1) == "start") {
			Swarm.spawn(frrThread);
		}
		while(true) {
			Thread.sleep(1000);
		}
	}
	
	def frrThread(u : Unit) = {
		println("1");
		val vLoc = Ref("test local string");
		println("2");
		val vRem = Ref(new Location(myLocation.address, 9997), "test remote string");
		println("3");
		println(vLoc());
		println("4");
		println(vRem());
		println("5");
		NoBee()
	}
}