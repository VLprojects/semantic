package pro.vlprojects.semantic.delivery.outbox

import java.time.Duration

public data class OutboxStalenessProperties(public val started: Duration = Duration.ofMinutes(1)) {

    init {
        require(started.isPositive) { "The staleness of a started delivery must be positive: $started" }
    }
}
