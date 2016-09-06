package org.swarm.examples.experiments

import java.io.Serializable
import kotlin.concurrent.thread

fun naivelyThreadedCoroutine(coroutine coroutineBlock: NaivelyThreadedController.() -> Continuation<Unit>) {
    NaivelyThreadedController().coroutineBlock().resume(Unit)
}

class NaivelyThreadedController : Serializable {
    suspend fun <T> doWork(block: () -> T, continuation: Continuation<T>) {
        thread {
            continuation.resume(block())
        }
    }
}
