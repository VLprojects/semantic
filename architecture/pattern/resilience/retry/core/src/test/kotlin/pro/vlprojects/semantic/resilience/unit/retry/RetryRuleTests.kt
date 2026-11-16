package pro.vlprojects.semantic.resilience.unit.retry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.retry.RetryCondition
import pro.vlprojects.semantic.resilience.retry.RetryDecision
import pro.vlprojects.semantic.resilience.retry.RetryRule
import pro.vlprojects.semantic.resilience.retry.RetryStopReason
import java.io.IOException
import java.time.Duration
import java.util.concurrent.CancellationException

class RetryRuleTests {

    private val delay = Duration.ofSeconds(1)
    private val backoff = FixedBackoff(delay)

    @Test
    fun `a failure with attempts left waits for the backoff`() {
        val rule = RetryRule.of(backoff, attemptLimit = 3)

        assertEquals(RetryDecision.Wait(delay), rule.decide(2, Duration.ZERO, IOException()))
    }

    @Test
    fun `the last attempt stops`() {
        val rule = RetryRule.of(backoff, attemptLimit = 3)

        assertEquals(stopBecause(RetryStopReason.ATTEMPTS_EXHAUSTED), rule.decide(3, Duration.ZERO, IOException()))
    }

    @Test
    fun `a delay that does not fit the time limit stops`() {
        val rule = RetryRule.of(backoff, timeLimit = Duration.ofSeconds(5))

        assertEquals(RetryDecision.Wait(delay), rule.decide(1, Duration.ofSeconds(4), IOException()))
        assertEquals(
            stopBecause(RetryStopReason.TIME_EXHAUSTED),
            rule.decide(1, Duration.ofSeconds(4).plusMillis(1), IOException()),
        )
    }

    @Test
    fun `a failure the condition refuses stops`() {
        val rule = RetryRule.of(backoff, condition = RetryCondition.including(IOException::class))

        assertEquals(stopBecause(RetryStopReason.NOT_RETRYABLE), rule.decide(1, Duration.ZERO, IllegalStateException()))
    }

    @Test
    fun `an error and a cancellation are never repeated`() {
        val rule = RetryRule.of(backoff)

        assertEquals(stopBecause(RetryStopReason.NOT_RETRYABLE), rule.decide(1, Duration.ZERO, OutOfMemoryError()))
        assertEquals(stopBecause(RetryStopReason.NOT_RETRYABLE), rule.decide(1, Duration.ZERO, CancellationException()))
    }

    @Test
    fun `an interruption stops`() {
        val rule = RetryRule.of(backoff)

        assertEquals(stopBecause(RetryStopReason.INTERRUPTED), rule.decide(1, Duration.ZERO, InterruptedException()))
    }

    @Test
    fun `limits that are not positive are refused`() {
        assertThrows<IllegalArgumentException> { RetryRule.of(backoff, attemptLimit = 0) }
        assertThrows<IllegalArgumentException> { RetryRule.of(backoff, timeLimit = Duration.ZERO) }
    }

    private fun stopBecause(reason: RetryStopReason) = RetryDecision.Stop(reason)
}
