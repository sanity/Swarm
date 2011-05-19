package swarm

import data.Ref
import org.scalatest.FunSuite

class FutureTest extends FunSuite {

  test("Future") {

    val result = Swarm.spawnAndReturn {
      Swarm.moveTo(InMemLocation(1))

      // create a Ref
      val ref2 = Ref(InMemLocation(2), "test string two")
      assert(InMemTest.currentLocation === Some(InMemLocation(2)))

      ref2()
    }(InMemTest.getTransporter, InMemLocation(1))

    assert("test string two" === result)
  }
}