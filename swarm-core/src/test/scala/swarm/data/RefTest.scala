package swarm.data

import org.scalatest.FunSuite
import swarm.Swarm.swarm
import util.continuations._
import swarm.transport.{Location, Transporter}
import swarm._

class RefTest extends FunSuite {

  def execute(f: Unit => Bee@swarm) {
    Swarm.execute(reset(f()))(InMemTest.getTransporter)
  }

  test("RefMap") {
    execute {
      Unit =>

        implicit val tx: Transporter = InMemTest.tx1
        implicit val local: Location = InMemLocation(1)

        RefMap.locations = List(InMemLocation(1), InMemLocation(2))

        val stringsMap: RefMap[String] = RefMap(classOf[String], "strings")

        stringsMap.put(InMemLocation(1), "one", "1")

        stringsMap.put(InMemLocation(2), "two", "2")

        Thread.sleep(100)

        Swarm.moveTo(InMemLocation(1))
        assert(Some("1") === stringsMap.get("one"))
        assert(Some(InMemLocation(1)) === InMemTest.currentLocation)

        assert(Some("2") === stringsMap.get("two"))
        assert(Some(InMemLocation(2)) === InMemTest.currentLocation)

        assert(Some("1") === RefMap(classOf[String], "strings").get("one"))
        assert(Some(InMemLocation(1)) === InMemTest.currentLocation)

        NoBee()
    }
  }
}