package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public class OutboxPollingContext private constructor(
    public val dispatched: Int,
    public val batchSize: Int,
    public val previousDurationBeforePolling: Duration,
) {

    init {
        require(batchSize >= 1) { "The batch size must be positive: $batchSize" }
        require(dispatched in 0..batchSize) { "The dispatched count $dispatched is outside the batch size $batchSize" }
        require(!previousDurationBeforePolling.isNegative) {
            "The previous duration before polling must not be negative: $previousDurationBeforePolling"
        }
    }

    public fun isBatchFull(): Boolean = dispatched == batchSize

    public companion object Factory {

        public fun of(
            dispatched: Int,
            batchSize: Int,
            previousDurationBeforePolling: Duration,
        ): OutboxPollingContext = OutboxPollingContext(dispatched, batchSize, previousDurationBeforePolling)
    }
}
