package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.format.abbreviation.SuffixAbbreviation
import pro.vlprojects.semantic.format.abbreviation.abbreviated
import java.time.Instant

private const val MAX_ERROR_LENGTH = 1000

@ConsistentCopyVisibility
public data class Delivery private constructor(
    public val scheduledAt: Instant,
    public val status: DeliveryStatus,
    public val attempts: Int,
    public val startedAt: Instant?,
    public val terminatedAt: Instant?,
    public val error: String?,
) {

    internal fun completed(completedAt: Instant): Delivery {
        checkHasStartedStatus()

        return copy(status = DeliveryStatus.COMPLETED, terminatedAt = completedAt, error = null)
    }

    internal fun failed(scheduledAt: Instant, error: String): Delivery {
        checkHasStartedStatus()
        val abbreviatedError = error.abbreviated(
            minLength = MAX_ERROR_LENGTH,
            visibleLength = MAX_ERROR_LENGTH - 1,
            abbreviation = SuffixAbbreviation,
        )

        return copy(
            scheduledAt = scheduledAt,
            status = DeliveryStatus.FAILED,
            error = abbreviatedError,
        )
    }

    internal fun canceled(canceledAt: Instant): Delivery {
        checkHasFailedStatus()

        return copy(status = DeliveryStatus.CANCELED, terminatedAt = canceledAt)
    }

    private fun checkHasStartedStatus() = check(status == DeliveryStatus.STARTED) { "Delivery is $status" }

    private fun checkHasFailedStatus() = check(status == DeliveryStatus.FAILED) { "Delivery is $status" }

    public companion object Factory {

        public fun of(scheduledAt: Instant): Delivery = Delivery(
            scheduledAt = scheduledAt,
            status = DeliveryStatus.PENDING,
            attempts = 0,
            startedAt = null,
            terminatedAt = null,
            error = null,
        )

        public fun of(
            scheduledAt: Instant,
            status: DeliveryStatus,
            attempts: Int,
            startedAt: Instant?,
            terminatedAt: Instant?,
            error: String?,
        ): Delivery = Delivery(scheduledAt, status, attempts, startedAt, terminatedAt, error)
    }
}
