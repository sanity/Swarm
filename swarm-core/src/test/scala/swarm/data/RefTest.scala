package swarm.data

import org.scalatest.FunSuite
import swarm.transport.{Location, Transporter}
import swarm._
import collection.RefMap

class RefTest extends FunSuite {

  test("RefMap") {
    implicit val tx: Transporter = InMemTest.tx1
    implicit val local: Location = InMemLocation(1)

    Swarm.spawn {
      RefMap.add(InMemLocation(1))
      RefMap.add(InMemLocation(2))

      val stringsMap = RefMap(classOf[String], "strings")

      stringsMap.put(InMemLocation(1), "one", "1")
      Thread.sleep(100)

      stringsMap.put(InMemLocation(2), "two", "2")
      Thread.sleep(100)

      assert(Some("1") === stringsMap.get("one"))
      assert(Some("2") === stringsMap.get("two"))
      assert(Some("1") === RefMap(classOf[String], "strings").get("one"))
    }
  }
}