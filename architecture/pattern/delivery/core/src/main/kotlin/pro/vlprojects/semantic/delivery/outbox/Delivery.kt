package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public class Delivery(
    public val scheduledAt: Instant,
    public val status: DeliveryStatus = DeliveryStatus.AWAITING,
    public val attempts: Int = 0,
    public val lockedUntil: Instant? = null,
    public val completedAt: Instant? = null,
    public val failureReason: String? = null,
) {

    internal fun start(lockedUntil: Instant): Delivery {
        check(!status.isTerminal) { "Delivery is already $status" }
        return Delivery(
            scheduledAt = scheduledAt,
            status = DeliveryStatus.IN_PROGRESS,
            attempts = attempts + 1,
            lockedUntil = lockedUntil,
            failureReason = failureReason,
        )
    }

    internal fun complete(completedAt: Instant): Delivery {
        checkInProgress()
        return Delivery(
            scheduledAt = scheduledAt,
            status = DeliveryStatus.COMPLETED,
            attempts = attempts,
            completedAt = completedAt,
        )
    }

    internal fun fail(scheduledAt: Instant, reason: String): Delivery {
        checkInProgress()
        return Delivery(
            scheduledAt = scheduledAt,
            status = DeliveryStatus.FAILED,
            attempts = attempts,
            failureReason = reason.take(MAX_REASON_LENGTH),
        )
    }

    internal fun abandon(reason: String): Delivery {
        checkInProgress()
        return Delivery(
            scheduledAt = scheduledAt,
            status = DeliveryStatus.ABANDONED,
            attempts = attempts,
            failureReason = reason.take(MAX_REASON_LENGTH),
        )
    }

    private fun checkInProgress() = check(status == DeliveryStatus.IN_PROGRESS) { "Delivery is $status" }

    private companion object {

        const val MAX_REASON_LENGTH = 1000
    }
}
