package swarm.demos

import swarm.InetSwarm

object Listen {
	def main(args: Array[String]) = {
		InetSwarm.listen(java.lang.Short.parseShort(args(0)))
		while(true) {
			Thread.sleep(1000)
		}
	}
}