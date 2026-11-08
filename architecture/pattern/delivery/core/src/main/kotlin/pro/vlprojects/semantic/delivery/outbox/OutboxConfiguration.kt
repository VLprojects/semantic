package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import java.time.Duration

public class OutboxConfiguration private constructor(
    public val lockDuration: Duration,
    public val maxAttempts: Int,
    public val backoff: Backoff,
    public val errorPolicy: ErrorPolicy,
) {

    init {
        require(lockDuration.isPositive) { "The lock duration must be positive: $lockDuration" }
        require(maxAttempts >= 1) { "The maximum of attempts must be positive: $maxAttempts" }
    }

    public companion object Factory {

        private const val DEFAULT_MAX_ATTEMPTS = 5
        private val DEFAULT_LOCK_DURATION = Duration.ofMinutes(1)

        public fun of(
            lockDuration: Duration = DEFAULT_LOCK_DURATION,
            maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
            backoff: Backoff = ExponentialBackoff.of(),
            errorPolicy: ErrorPolicy = RootCauseErrorPolicy,
        ): OutboxConfiguration = OutboxConfiguration(lockDuration, maxAttempts, backoff, errorPolicy)
    }
}
