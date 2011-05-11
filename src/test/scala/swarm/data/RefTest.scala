package swarm.data

import org.scalatest.FunSuite
import swarm.Swarm.swarm
import util.continuations._
import swarm.InMemLocation._
import swarm._

class RefTest extends FunSuite {

  def execute(f: Unit => Bee@swarm) {
    Swarm.execute(reset(f()))(InMemTest.getTransporter)
  }

  test("RefMap") {
    execute {
      Unit =>
        RefMap.local(InMemLocation(1))

        val key = "my key"
        val value = "my value"

        assert(RefMap.get(classOf[String], key) === None)

        RefMap.put(key, value)

        assert(RefMap.get(classOf[String], key) === Some(value))

        NoBee()
    }
  }
}