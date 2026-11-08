package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

internal class BackoffLimits(private val maxAttempts: Int?, deadline: Duration?) {

    init {
        maxAttempts?.let { require(it >= 1) { "The max attempts must be positive: $it" } }
        deadline?.let { require(it.isPositive) { "The deadline must be positive: $it" } }
    }

    private val deadlineNanos = deadline?.toNanos()?.toDouble()

    fun verifyNotExceededAfter(attempt: Int, totalDelayNanos: Double) {
        when {
            isOutOfAttempts(attempt) -> throw BackoffLimitExceededException(attempt)
            isPastDeadline(totalDelayNanos) -> throw BackoffLimitExceededException(attempt)
        }
    }

    private fun isOutOfAttempts(attempt: Int) = maxAttempts?.let { attempt >= it } == true

    private fun isPastDeadline(totalDelayNanos: Double) = deadlineNanos?.let { totalDelayNanos > it } == true
}
