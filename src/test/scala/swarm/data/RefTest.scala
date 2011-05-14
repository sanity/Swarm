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
        RefMap.locations(List(InMemLocation(2)))

        Swarm.moveTo(InMemLocation(1))
        RefMap.put("one", "1")
        Swarm.moveTo(InMemLocation(2))
        RefMap.put("two", "2")

        val string1: Option[String] = RefMap.get(classOf[String], "one")
        Swarm.moveTo(InMemLocation(1))
        assert(Some("1") === string1)
        assert(Some(InMemLocation(1)) === InMemTest.currentLocation)

        val string2: Option[String] = RefMap.get(classOf[String], "two")
        assert(Some("2") === string2)
        // TODO this won't work because there's only one RefMap in this test (since we're using in-memory locations)
        //assert(Some(InMemLocation(2)) === InMemTest.currentLocation)

        NoBee()
    }
  }
}