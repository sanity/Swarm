package swarm

import data.Ref
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers._
import java.util.UUID
import swarm.Swarm._

class FutureTest extends FunSuite {

  test("Future") {
    val uuid = UUID.randomUUID.toString
    spawn {

      moveTo(InMemLocation(1))
      val ref2 = Ref(InMemLocation(2), "test string two")
      saveFutureResult(uuid, ref2())

    }(InMemTest.getTransporter)

    getFutureResult(uuid) should equal("test string two")
  }
}