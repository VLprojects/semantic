package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public class OutboxPollingContext private constructor(
    public val dispatched: Int,
    public val batchSize: Int,
    public val previousDelay: Duration,
) {

    init {
        require(batchSize >= 1) { "The batch size must be positive: $batchSize" }
        require(dispatched in 0..batchSize) { "The dispatched count $dispatched is outside the batch size $batchSize" }
        require(!previousDelay.isNegative) { "The previous delay must not be negative: $previousDelay" }
    }

    public fun isBatchFull(): Boolean = dispatched == batchSize

    public companion object Factory {

        public fun of(
            dispatched: Int,
            batchSize: Int,
            previousDelay: Duration,
        ): OutboxPollingContext = OutboxPollingContext(dispatched, batchSize, previousDelay)
    }
}
