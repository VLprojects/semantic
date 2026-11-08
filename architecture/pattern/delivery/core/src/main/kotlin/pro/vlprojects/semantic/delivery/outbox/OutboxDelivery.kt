package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public class OutboxDelivery private constructor(
    public val status: DeliveryStatus,
    public val attempts: Int,
    public val scheduledAt: Instant,
    public val lockedUntil: Instant?,
    public val completedAt: Instant?,
    public val failureReason: String?,
) {

    internal fun start(lockedUntil: Instant): OutboxDelivery {
        check(!status.isTerminal) { "Delivery is already $status" }
        return OutboxDelivery(DeliveryStatus.IN_PROGRESS, attempts + 1, scheduledAt, lockedUntil, null, failureReason)
    }

    internal fun complete(completedAt: Instant): OutboxDelivery {
        checkInProgress()
        return OutboxDelivery(DeliveryStatus.COMPLETED, attempts, scheduledAt, null, completedAt, null)
    }

    internal fun fail(scheduledAt: Instant, reason: String): OutboxDelivery {
        checkInProgress()
        return OutboxDelivery(DeliveryStatus.FAILED, attempts, scheduledAt, null, null, reason.take(MAX_REASON_LENGTH))
    }

    internal fun abandon(reason: String): OutboxDelivery {
        checkInProgress()
        return OutboxDelivery(
            DeliveryStatus.ABANDONED,
            attempts,
            scheduledAt,
            null,
            null,
            reason.take(MAX_REASON_LENGTH),
        )
    }

    private fun checkInProgress() = check(status == DeliveryStatus.IN_PROGRESS) { "Delivery is $status" }

    public companion object {

        private const val MAX_REASON_LENGTH = 1000

        internal fun schedule(scheduledAt: Instant) =
            OutboxDelivery(DeliveryStatus.AWAITING, 0, scheduledAt, null, null, null)

        public fun restore(
            status: DeliveryStatus,
            attempts: Int,
            scheduledAt: Instant,
            lockedUntil: Instant?,
            completedAt: Instant?,
            failureReason: String?,
        ): OutboxDelivery = OutboxDelivery(status, attempts, scheduledAt, lockedUntil, completedAt, failureReason)
    }
}
