package swarm.data

import org.scalatest.FunSuite
import swarm.transport.{Location, Transporter}
import swarm._

class RefTest extends FunSuite {

  test("RefMap") {
    implicit val tx: Transporter = InMemTest.tx1
    implicit val local: Location = InMemLocation(1)

    RefMap.locations = List(InMemLocation(1), InMemLocation(2))

    val stringsMap = RefMap.get(classOf[String], "strings")

    stringsMap.put(InMemLocation(1), "one", "1")
    Thread.sleep(100)

    stringsMap.put(InMemLocation(2), "two", "2")
    Thread.sleep(100)

    assert(Some("1") === stringsMap.get("one"))
    assert(Some("2") === stringsMap.get("two"))
    assert(Some("1") === RefMap.get(classOf[String], "strings").get("one"))
  }

  test("continuations should accomodate foreach") {
    implicit val tx: Transporter = InMemTest.tx1
    implicit val local: Location = InMemLocation(1)

    RefMap.locations = List(InMemLocation(1), InMemLocation(2))

    val stringsMap = RefMap.get(classOf[String], "strings")

    List("hello", "world").foreach(string => stringsMap.put(InMemLocation(1), "string", string))
  }

  test("continuations should accomodate map") {
    implicit val tx: Transporter = InMemTest.tx1
    implicit val local: Location = InMemLocation(1)

    RefMap.locations = List(InMemLocation(1), InMemLocation(2))

    val stringsMap = RefMap.get(classOf[String], "strings")

    List("hello", "world").map(string => stringsMap.put(InMemLocation(1), "string", string))
  }
}