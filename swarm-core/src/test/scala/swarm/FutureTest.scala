package swarm

import data.Ref._
import data.{Ref, RefMap}
import org.scalatest.FunSuite
import swarm.Swarm.swarm
import util.continuations._

class FutureTest extends FunSuite {

  test("Future") {

    def execute(f: Unit => Bee@swarm) {
      Swarm.spawn(f)(InMemTest.getTransporter)
    }

    val uuid: String = java.util.UUID.randomUUID.toString
    val future: Future = Swarm.future(uuid)

    execute {
      Unit =>

        Swarm.moveTo(InMemLocation(1))

        // create a Ref
        val ref2 = Ref(InMemLocation(2), "test string two")
        assert(InMemTest.currentLocation === Some(InMemLocation(2)))

        val value = ref2()
        Swarm.moveTo(InMemLocation(1))
        Swarm.futureValue(uuid, value)

        NoBee()
    }

    val result = future.get

    assert("test string two" === result)
    
  }
}