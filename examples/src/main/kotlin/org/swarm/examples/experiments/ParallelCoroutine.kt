package org.swarm.examples.experiments

import java.io.Serializable
import kotlin.concurrent.thread

fun parallelCoroutine(coroutine coroutineBlock: ParallelController.() -> Continuation<Unit>) {
    ParallelController().coroutineBlock().resume(Unit)
}

class ParallelController : Serializable {
    suspend fun <T> doWork(block: () -> T, continuation: Continuation<T?>) {
        thread {
            block()
        }
        thread {
            continuation.resume(null)
        }
    }
}
