package pro.vlprojects.semantic.delivery.outbox

import java.time.Duration

public data class OutboxStalenessProperties(public val inProgress: Duration = Duration.ofMinutes(1)) {

    init {
        require(inProgress.isPositive) { "The staleness of a delivery in progress must be positive: $inProgress" }
    }
}
