package swarm

import data.Ref
import org.scalatest.FunSuite
import java.util.UUID

class FutureTest extends FunSuite {

  test("Future") {
    val uuid = UUID.randomUUID.toString
    Swarm.spawn {
      Swarm.moveTo(InMemLocation(1))
      val ref2 = Ref(InMemLocation(2), "test string two")
      Swarm.saveFutureResult(uuid, ref2())
    }(InMemTest.getTransporter)

    assert("test string two" === Swarm.getFutureResult(uuid))
  }
}