package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.delivery.outbox.error.ErrorPolicy
import pro.vlprojects.semantic.delivery.outbox.error.RootCauseErrorPolicy
import pro.vlprojects.semantic.delivery.outbox.logging.OutboxLoggingProperties
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff

public data class OutboxProperties(
    public val staleness: OutboxStalenessProperties = OutboxStalenessProperties(),
    public val maxAttempts: Int = 5,
    public val backoff: Backoff = ExponentialBackoff.of(),
    public val errorPolicy: ErrorPolicy = RootCauseErrorPolicy(),
    public val batchSize: Int = 100,
    public val logging: OutboxLoggingProperties = OutboxLoggingProperties(),
) {

    init {
        require(batchSize >= 1) { "The batch size must be positive: $batchSize" }
        require(maxAttempts >= 1) { "The maximum of attempts must be positive: $maxAttempts" }
    }
}
