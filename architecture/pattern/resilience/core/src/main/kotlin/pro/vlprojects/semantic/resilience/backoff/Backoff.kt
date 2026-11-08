package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

public interface Backoff {

    public val maxAttempts: Int?

    public val deadline: Duration?

    @Throws(BackoffLimitExceededException::class)
    public fun calculateDelay(attempt: Int): Duration
}
