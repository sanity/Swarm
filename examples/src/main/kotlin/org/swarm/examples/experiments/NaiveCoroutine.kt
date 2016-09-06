package org.swarm.examples.experiments

import java.io.Serializable

class NaiveController : Serializable {
    suspend fun <T> doWork(block: () -> T, continuation: Continuation<T>) {
        continuation.resume(block())
    }
}

fun naiveCoroutine(coroutine coroutineBlock: NaiveController.() -> Continuation<Unit>) {
    NaiveController().coroutineBlock().resume(Unit)
}

