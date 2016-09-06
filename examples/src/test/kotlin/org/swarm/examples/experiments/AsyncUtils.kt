package org.swarm.examples.experiments

val performLongRunningTask = { duration: Long, label: Int ->
    println("starting doing task #$label on thread: ${Thread.currentThread()}")
    Thread.sleep(duration)
    println("finished doing task #$label on thread: ${Thread.currentThread()}")
    println("=======")
}