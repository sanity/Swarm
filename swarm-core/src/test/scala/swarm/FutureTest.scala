package swarm

import data.Ref
import org.scalatest.FunSuite
class FutureTest extends FunSuite {

  test("Future") {

    val uuid: String = java.util.UUID.randomUUID.toString
    val future: Future = Swarm.future(uuid)

    Swarm.spawn {
      Thread.sleep(1000)

      Swarm.moveTo(InMemLocation(1))

      // create a Ref
      val ref2 = Ref(InMemLocation(2), "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      val value = ref2()
      Swarm.moveTo(InMemLocation(1))
      Swarm.futureValue(uuid, value)
    }(InMemTest.getTransporter)

    val result = future.get

    assert("test string two" === result)

  }
}