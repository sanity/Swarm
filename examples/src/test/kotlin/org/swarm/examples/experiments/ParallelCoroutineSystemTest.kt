package org.swarm.examples.experiments

import com.memoizr.assertk.expect
import com.memoizrlabs.functional_kotlin_base.invoke
import org.junit.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import kotlin.system.measureTimeMillis


class ParallelCoroutineSystemTest {

    val individualTaskDuration = 100L
    val performTask = performLongRunningTask(individualTaskDuration)
    val threads = ConcurrentLinkedQueue<Long>()

    @Test
    fun `naive implementation executes continuation in series`() {
        val executionTime = measureTimeMillis {

            naiveCoroutine {
                doWork {
                    threads.add(currentThread())
                    performTask(1)
                }
                doWork {
                    threads.add(currentThread())
                    performTask(2)
                }
                doWork {
                    threads.add(currentThread())
                    performTask(3)
                }
            }

        }

        expect that executionTime isCloseTo 3 * individualTaskDuration withinPercentage 20
        expect that threads.all { it == threads.peek() } _is true
    }

    @Test
    fun `naive threaded implementation also executes continuations in series`() {
        val executionTime = measureTimeMillis {

            val latch = CountDownLatch(3)

            naivelyThreadedCoroutine {
                val result = doWork {
                    threads.add(currentThread())
                    performTask(1)
                    latch.countDown()
                    "result"
                }
                doWork {
                    threads.add(currentThread())
                    performTask(2)
                    latch.countDown()
                    expect that result isEqualTo "result"
                }
                doWork {
                    threads.add(currentThread())
                    performTask(3)
                    latch.countDown()
                }

            }

            latch.await()
        }

        expect that executionTime isCloseTo 3 * individualTaskDuration withinPercentage 20
        expect that threads containsOnlyOnce listOf(threads.peek())
    }

    @Test
    fun `parallel implementation executes continuations in parallel`() {
        val executionTime = measureTimeMillis {

            val latch = CountDownLatch(3)

            parallelCoroutine {
                doWork {
                    threads.add(currentThread())
                    performTask(1)
                    latch.countDown()
                }
                doWork {
                    threads.add(currentThread())
                    performTask(2)
                    latch.countDown()
                }
                doWork {
                    threads.add(currentThread())
                    performTask(3)
                    latch.countDown()
                }
            }

            latch.await()
        }

        expect that executionTime isCloseTo individualTaskDuration withinPercentage 20
        expect that threads containsOnlyOnce listOf(threads.peek())
    }

    @Test
    fun `parallel continuations cannot depend on the result of another continuation`() {
        val threads = ConcurrentLinkedQueue<Long>()
        val executionTime = measureTimeMillis {

            val latch = CountDownLatch(3)

            parallelCoroutine {
                val result = doWork {
                    threads.add(currentThread())
                    performTask(1)
                    latch.countDown()
                    "result"
                }
                doWork {
                    threads.add(currentThread())
                    performTask(2)
                    latch.countDown()

                    expect that result _is null
                }
                doWork {
                    threads.add(currentThread())
                    performTask(3)
                    latch.countDown()
                }
            }

            latch.await()
        }

        expect that executionTime isCloseTo individualTaskDuration withinPercentage 20
        expect that threads containsOnlyOnce listOf(threads.peek())
    }

    private fun currentThread() = Thread.currentThread().id
}

