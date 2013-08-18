package org.swarmframework.tests

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers._

import org.swarmframework.transport._
import org.swarmframework.core.Swarm._
import org.swarmframework.collection.RefMap

class RefTest extends FunSuite {

  test("RefMap") {
    implicit val tx: Transporter = InMemTest.tx1
    implicit val local: Location = InMemLocation(1)

    spawn {
      RefMap.add(InMemLocation(1))
      RefMap.add(InMemLocation(2))

      val stringsMap = RefMap(classOf[String], "strings")

      stringsMap.put(InMemLocation(1), "refmap-test-one", "1")
      Thread.sleep(100)

      stringsMap.put(InMemLocation(2), "refmap-test-two", "2")
      Thread.sleep(100)

      val oneResult = stringsMap.get("refmap-test-one")
      val twoResult = stringsMap.get("refmap-test-two")
      val refMapResult = RefMap(classOf[String], "strings").get("refmap-test-one")
      oneResult should equal(Some("1"))
      twoResult should equal(Some("2"))
      refMapResult should equal(Some("1"))
    }
  }
}