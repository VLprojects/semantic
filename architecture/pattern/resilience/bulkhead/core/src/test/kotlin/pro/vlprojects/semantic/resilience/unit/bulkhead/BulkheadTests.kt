package pro.vlprojects.semantic.resilience.unit.bulkhead

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.bulkhead.Bulkhead
import pro.vlprojects.semantic.resilience.bulkhead.BulkheadFullException
import pro.vlprojects.semantic.resilience.bulkhead.BulkheadListener
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class BulkheadTests {

    private val longWait = Duration.ofMinutes(1)
    private val rejections = CopyOnWriteArrayList<Duration>()
    private val rejectionListener = object : BulkheadListener {

        override fun onRejected(bulkhead: Bulkhead, waited: Duration) {
            rejections += waited
        }
    }
    private val failingListener = object : BulkheadListener {

        override fun onPermitted(bulkhead: Bulkhead, waited: Duration) = error("broken")

        override fun onRejected(bulkhead: Bulkhead, waited: Duration) = error("broken")

        override fun onFinished(bulkhead: Bulkhead) = error("broken")
    }

    @Test
    fun `the result of the action is returned and the place is freed`() {
        val bulkhead = Bulkhead.of(limit = 1)

        val result = bulkhead.execute { "done" }

        assertEquals("done", result)
        assertEquals(0, bulkhead.activeCalls)
    }

    @Test
    fun `the failure of the action reaches the caller and the place is freed`() {
        val bulkhead = Bulkhead.of(limit = 1)

        assertThrows<IllegalStateException> {
            bulkhead.execute { error("broken") }
        }

        assertEquals(0, bulkhead.activeCalls)
    }

    @Test
    fun `a call is refused at once when there is no place and no wait`() {
        val bulkhead = Bulkhead.of(limit = 1, name = "payments")
        val release = occupy(bulkhead)

        val failure = assertThrows<BulkheadFullException> { bulkhead.execute { } }

        assertEquals("payments", failure.bulkheadName)
        assertEquals(1, failure.limit)
        assertEquals(Duration.ZERO, failure.waitLimit)
        assertInstanceOf(RejectedExecutionException::class.java, failure)
        release.countDown()
    }

    @Test
    fun `a call takes a place that is freed within the wait limit`() {
        val bulkhead = Bulkhead.of(limit = 1, waitLimit = longWait)
        val release = occupy(bulkhead)
        val result = AtomicReference<String>()
        val caller = thread {
            bulkhead
                .execute { "done" }
                .let(result::set)
        }

        awaitWaitingCall(bulkhead)
        release.countDown()
        caller.join()

        assertEquals("done", result.get())
    }

    @Test
    fun `a call is refused when the wait limit runs out`() {
        val waitLimit = Duration.ofMillis(20)
        val listeners = listOf(rejectionListener)
        val bulkhead = Bulkhead.of(limit = 1, waitLimit = waitLimit, listeners = listeners)
        val release = occupy(bulkhead)

        assertThrows<BulkheadFullException> { bulkhead.execute { } }

        val waited = rejections.single()
        assertTrue(waited >= waitLimit)
        release.countDown()
    }

    @Test
    fun `an interrupted wait is not a refusal`() {
        val listeners = listOf(rejectionListener)
        val bulkhead = Bulkhead.of(limit = 1, waitLimit = longWait, listeners = listeners)
        val release = occupy(bulkhead)
        val failure = AtomicReference<Throwable>()
        val caller = thread {
            runCatching { bulkhead.execute { } }
                .onFailure(failure::set)
        }

        awaitWaitingCall(bulkhead)
        caller.interrupt()
        caller.join()

        val thrown = failure.get()
        assertInstanceOf(InterruptedException::class.java, thrown)
        assertEquals(0, rejections.size)
        assertEquals(1, bulkhead.activeCalls)
        release.countDown()
    }

    @Test
    fun `an interrupted thread is not let in even when there is a place`() {
        val bulkhead = Bulkhead.of(limit = 1)

        Thread.currentThread().interrupt()

        assertThrows<InterruptedException> { bulkhead.execute { } }
        assertEquals(0, bulkhead.activeCalls)
    }

    @Test
    fun `listeners hear a call from the start to the end, the place freed by then`() {
        val events = CopyOnWriteArrayList<String>()
        val listener = object : BulkheadListener {

            override fun onPermitted(bulkhead: Bulkhead, waited: Duration) {
                events += "permitted ${bulkhead.activeCalls}"
            }

            override fun onFinished(bulkhead: Bulkhead) {
                events += "finished ${bulkhead.activeCalls}"
            }
        }
        val listeners = listOf(listener)
        val bulkhead = Bulkhead.of(limit = 1, listeners = listeners)

        bulkhead.execute { events += "action" }

        val expected = listOf("permitted 1", "action", "finished 0")
        assertEquals(expected, events)
    }

    @Test
    fun `a failing listener changes neither the result nor the place`() {
        val listeners = listOf(failingListener)
        val bulkhead = Bulkhead.of(limit = 1, listeners = listeners)

        val result = bulkhead.execute { "done" }

        assertEquals("done", result)
        assertEquals(0, bulkhead.activeCalls)
    }

    @Test
    fun `a failing listener does not hide a refusal from the next one`() {
        val listeners = listOf(failingListener, rejectionListener)
        val bulkhead = Bulkhead.of(limit = 1, listeners = listeners)
        val release = occupy(bulkhead)

        assertThrows<BulkheadFullException> { bulkhead.execute { } }

        assertEquals(1, rejections.size)
        release.countDown()
    }

    @Test
    fun `an interruption inside a listener stays on the thread`() {
        val listener = object : BulkheadListener {

            override fun onPermitted(bulkhead: Bulkhead, waited: Duration) = throw InterruptedException()
        }
        val listeners = listOf(listener)
        val bulkhead = Bulkhead.of(limit = 1, listeners = listeners)

        val isInterrupted = bulkhead.execute { Thread.interrupted() }

        assertTrue(isInterrupted)
    }

    @Test
    fun `a list of listeners changed later does not reach the bulkhead`() {
        val listeners = mutableListOf<BulkheadListener>()
        val bulkhead = Bulkhead.of(limit = 1, listeners = listeners)
        val release = occupy(bulkhead)

        listeners += rejectionListener
        assertThrows<BulkheadFullException> { bulkhead.execute { } }

        assertEquals(0, rejections.size)
        release.countDown()
    }

    @Test
    fun `a wait limit too long for nanoseconds is accepted`() {
        val waitLimit = Duration.ofSeconds(Long.MAX_VALUE)
        val bulkhead = Bulkhead.of(limit = 1, waitLimit = waitLimit)

        val result = bulkhead.execute { "done" }

        assertEquals("done", result)
    }

    @Test
    fun `a limit that is not positive is refused`() {
        assertThrows<IllegalArgumentException> { Bulkhead.of(limit = 0) }
    }

    @Test
    fun `a negative wait limit is refused`() {
        val waitLimit = Duration.ofMillis(-1)

        assertThrows<IllegalArgumentException> { Bulkhead.of(limit = 1, waitLimit = waitLimit) }
    }

    @Test
    fun `the name is shown`() {
        val bulkhead = Bulkhead.of(limit = 1, name = "payments")

        assertEquals("Bulkhead 'payments'", bulkhead.toString())
    }

    private fun occupy(bulkhead: Bulkhead): CountDownLatch {
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        thread(isDaemon = true) {
            bulkhead.execute {
                entered.countDown()
                release.await()
            }
        }
        val isEntered = entered.await(1, TimeUnit.MINUTES)
        assertTrue(isEntered)
        return release
    }

    private fun awaitWaitingCall(bulkhead: Bulkhead) {
        val deadline = System.nanoTime() + longWait.toNanos()
        while (bulkhead.waitingCalls == 0) {
            val isBeforeDeadline = System.nanoTime() < deadline
            check(isBeforeDeadline) { "No call started to wait" }
            Thread.onSpinWait()
        }
    }
}
