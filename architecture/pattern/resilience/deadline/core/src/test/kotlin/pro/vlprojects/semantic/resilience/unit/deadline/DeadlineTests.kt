package pro.vlprojects.semantic.resilience.unit.deadline

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.deadline.Deadline
import pro.vlprojects.semantic.resilience.deadline.DeadlineExceededException
import pro.vlprojects.semantic.resilience.deadline.DeadlineListener
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class DeadlineTests {

    private val events = CopyOnWriteArrayList<String>()
    private val overrun = CountDownLatch(1)
    private val listener = object : DeadlineListener {

        override fun onCompleted(elapsed: Duration) {
            events += "completed"
        }

        override fun onFailed(elapsed: Duration, failure: Throwable) {
            events += "failed"
        }

        override fun onExpired(limit: Duration) {
            events += "expired"
        }

        override fun onOverrun(elapsed: Duration) {
            events += "overrun"
            overrun.countDown()
        }
    }
    private val patient = Deadline(Duration.ofMinutes(1), listeners = listOf(listener))
    private val hasty = Deadline(Duration.ofMillis(50), "payments", listOf(listener))

    @Test
    fun `an action finished in time gives its result`() {
        assertEquals("answer", patient.execute { "answer" })
        assertEquals(listOf("completed"), events)
    }

    @Test
    fun `an action runs in a virtual thread`() {
        assertTrue(patient.execute { Thread.currentThread().isVirtual })
    }

    @Test
    fun `a failure in time is the same exception`() {
        val failure = IllegalStateException("broken")

        val thrown = assertThrows<IllegalStateException> { patient.execute { throw failure } }

        assertSame(failure, thrown)
        assertEquals(listOf("failed"), events)
    }

    @Test
    fun `an expired call names its deadline and limit`() {
        val thrown = assertThrows<DeadlineExceededException> { hasty.execute { awaitInterrupt() } }

        assertEquals("payments", thrown.name)
        assertEquals(Duration.ofMillis(50), thrown.limit)
    }

    @Test
    fun `an expired call is interrupted and reports the overrun`() {
        assertThrows<DeadlineExceededException> { hasty.execute { awaitInterrupt() } }

        assertTrue(overrun.await(1, TimeUnit.MINUTES))
        assertEquals(listOf("expired", "overrun"), events.sorted())
    }

    @Test
    fun `an action ignoring the interrupt does not hold the caller`() {
        val release = CountDownLatch(1)

        assertThrows<DeadlineExceededException> { hasty.execute { awaitIgnoringInterrupt(release) } }

        assertEquals(listOf("expired"), events)
        release.countDown()
        assertTrue(overrun.await(1, TimeUnit.MINUTES))
    }

    @Test
    fun `an interrupted caller interrupts the action`() {
        val interrupted = CountDownLatch(1)
        Thread.currentThread().interrupt()

        assertThrows<InterruptedException> {
            patient.execute {
                runCatching(::awaitInterrupt)
                interrupted.countDown()
            }
        }

        assertTrue(interrupted.await(1, TimeUnit.MINUTES))
        assertEquals(emptyList<String>(), events)
    }

    @Test
    fun `a failing listener does not change the result`() {
        val failing = object : DeadlineListener {

            override fun onCompleted(elapsed: Duration): Unit = error("broken listener")
        }

        assertEquals("answer", Deadline(Duration.ofMinutes(1), listeners = listOf(failing)).execute { "answer" })
    }

    @Test
    fun `a limit must be positive`() {
        assertThrows<IllegalArgumentException> { Deadline.of(Duration.ZERO) }
    }

    private fun awaitInterrupt() = CountDownLatch(1).await()

    private fun awaitIgnoringInterrupt(release: CountDownLatch) {
        while (release.count > 0) {
            runCatching { release.await() }
        }
    }
}
