package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

internal class BackoffLimits(private val maxAttempts: Int?, deadline: Duration?) {

    init {
        maxAttempts?.let { require(it >= 1) { "The max attempts must be positive: $it" } }
        deadline?.let { require(it.isPositive) { "The deadline must be positive: $it" } }
    }

    private val deadlineNanos = deadline?.toNanos()?.toDouble()

    fun check(attempt: Int, waitedNanos: Double) {
        when {
            isLastAttempt(attempt) -> throw BackoffLimitExceededException(attempt)
            hasDeadlinePassed(waitedNanos) -> throw BackoffLimitExceededException(attempt)
        }
    }

    private fun isLastAttempt(attempt: Int) = maxAttempts != null && attempt >= maxAttempts

    private fun hasDeadlinePassed(waitedNanos: Double) = deadlineNanos != null && waitedNanos > deadlineNanos
}
