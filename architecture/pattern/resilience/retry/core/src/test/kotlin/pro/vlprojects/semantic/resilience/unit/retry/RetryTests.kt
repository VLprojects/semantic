package pro.vlprojects.semantic.resilience.unit.retry

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.retry.Retry
import pro.vlprojects.semantic.resilience.retry.RetryListener
import pro.vlprojects.semantic.resilience.retry.RetryProgress
import pro.vlprojects.semantic.resilience.retry.RetryRule
import pro.vlprojects.semantic.resilience.retry.RetryStopReason
import pro.vlprojects.semantic.resilience.retry.Sleeper
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.InstantSource

class RetryTests {

    private val delay = Duration.ofSeconds(1)
    private var now = Instant.EPOCH
    private val instantSource = InstantSource { now }
    private val advancing = Sleeper { pause -> now += pause }
    private val events = mutableListOf<String>()
    private val listener = object : RetryListener {
        override fun onRetry(progress: RetryProgress, failure: Throwable, delay: Duration) {
            events += "retry ${progress.attempt}"
        }

        override fun onSuccess(progress: RetryProgress) {
            events += "success ${progress.attempt} ${progress.elapsed}"
        }

        override fun onStop(progress: RetryProgress, failure: Throwable, reason: RetryStopReason) {
            events += "stop ${progress.attempt} $reason"
        }
    }

    @AfterEach
    fun clearInterruptFlag() {
        Thread.interrupted()
    }

    @Test
    fun `a first success returns at once`() {
        assertEquals("done", retry().execute { "done" })
        assertEquals(listOf("success 1 PT0S"), events)
    }

    @Test
    fun `a call that recovers is repeated with a pause`() {
        var calls = 0

        val result = retry().execute {
            calls++
            if (calls < 3) throw IOException()
            "done"
        }

        assertEquals("done", result)
        assertEquals(listOf("retry 1", "retry 2", "success 3 PT2S"), events)
    }

    @Test
    fun `the original failure leaves after the last attempt`() {
        val failure = IOException()

        val thrown = assertThrows<IOException> { retry().execute { throw failure } }

        assertSame(failure, thrown)
        assertEquals(listOf("retry 1", "retry 2", "stop 3 ATTEMPTS_EXHAUSTED"), events)
    }

    @Test
    fun `an interrupted thread starts no new attempt and keeps its flag`() {
        var calls = 0

        assertThrows<IOException> {
            retry().execute {
                calls++
                Thread.currentThread().interrupt()
                throw IOException()
            }
        }

        assertEquals(1, calls)
        assertEquals(listOf("stop 1 INTERRUPTED"), events)
        assertTrue(Thread.currentThread().isInterrupted)
    }

    @Test
    fun `an interrupted pause leaves with the failure and keeps the flag`() {
        val interrupted = Sleeper { throw InterruptedException() }
        val failure = IOException()

        val thrown = assertThrows<IOException> { retry(interrupted).execute { throw failure } }

        assertSame(failure, thrown)
        assertEquals(listOf("retry 1", "stop 1 INTERRUPTED"), events)
        assertTrue(Thread.currentThread().isInterrupted)
    }

    @Test
    fun `a blank name is refused`() {
        assertThrows<IllegalArgumentException> { Retry.of(" ") }
    }

    private fun retry(sleeper: Sleeper = advancing) = Retry(
        "profile",
        RetryRule.of(FixedBackoff(delay)),
        listener,
        sleeper,
        instantSource,
    )
}
