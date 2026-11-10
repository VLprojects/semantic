package pro.vlprojects.semantic.delivery.outbox

import org.slf4j.event.Level
import pro.vlprojects.semantic.delivery.outbox.error.ErrorPolicy
import pro.vlprojects.semantic.delivery.outbox.error.RootCauseErrorPolicy
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import java.time.Duration

public data class OutboxProperties(
    public val lockDuration: Duration = Duration.ofMinutes(1),
    public val maxAttempts: Int = 5,
    public val backoff: Backoff = ExponentialBackoff.of(),
    public val errorPolicy: ErrorPolicy = RootCauseErrorPolicy(),
    public val batchSize: Int = 100,
    public val failureLogLevel: Level = Level.WARN,
    public val cancellationLogLevel: Level = Level.ERROR,
) {

    init {
        require(batchSize >= 1) { "The batch size must be positive: $batchSize" }
        require(lockDuration.isPositive) { "The lock duration must be positive: $lockDuration" }
        require(maxAttempts >= 1) { "The maximum of attempts must be positive: $maxAttempts" }
    }
}
