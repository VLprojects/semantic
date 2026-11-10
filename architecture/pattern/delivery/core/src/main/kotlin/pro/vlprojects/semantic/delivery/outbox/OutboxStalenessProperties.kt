package pro.vlprojects.semantic.delivery.outbox

import java.time.Duration

public data class OutboxStalenessProperties(public val processing: Duration = Duration.ofMinutes(1)) {

    init {
        require(processing.isPositive) { "The staleness of a processing delivery must be positive: $processing" }
    }
}
