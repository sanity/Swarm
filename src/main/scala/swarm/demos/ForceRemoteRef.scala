package swarm.demos

import swarm._

object ForceRemoteRef {
	def main(args : Array[String]) = {
		InetSwarm.listen(java.lang.Short.parseShort(args(0)));
		if (args.length > 1 && args(1) == "start") {
			InetSwarm.spawn(frrThread);
		}
		while(true) {
			Thread.sleep(1000);
		}
	}
	
	def frrThread(u : Unit) = {
		
		println("1");
			
		val vLoc = InetRef("test local string");
		
		println("2");
			
		val vRem = InetRef(InetSwarm.local,
				       "test remote string");
		
		println("3");
			
		println(vLoc());
		
		println("4");
			
		println(vRem());
		
		println("5");
		
		NoBee()
	}
}