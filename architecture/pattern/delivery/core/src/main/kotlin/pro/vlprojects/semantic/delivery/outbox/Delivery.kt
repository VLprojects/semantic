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
    public val lockedUntil: Instant?,
    public val terminatedAt: Instant?,
    public val error: String?,
) {

    internal fun started(lockedUntil: Instant): Delivery {
        checkHasNotTerminalStatus()
        return copy(status = DeliveryStatus.STARTED, attempts = attempts + 1, lockedUntil = lockedUntil)
    }

    internal fun completed(terminatedAt: Instant): Delivery {
        checkHasStartedStatus()
        return copy(status = DeliveryStatus.COMPLETED, lockedUntil = null, terminatedAt = terminatedAt, error = null)
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
            lockedUntil = null,
            error = abbreviatedError,
        )
    }

    internal fun canceled(terminatedAt: Instant): Delivery {
        checkHasFailedStatus()
        return copy(status = DeliveryStatus.CANCELED, terminatedAt = terminatedAt)
    }

    private fun checkHasNotTerminalStatus() = check(!status.isTerminal) { "Delivery is already $status" }

    private fun checkHasStartedStatus() = check(status == DeliveryStatus.STARTED) { "Delivery is $status" }

    private fun checkHasFailedStatus() = check(status == DeliveryStatus.FAILED) { "Delivery is $status" }

    public companion object Factory {

        public fun of(
            scheduledAt: Instant,
            status: DeliveryStatus = DeliveryStatus.PENDING,
            attempts: Int = 0,
            lockedUntil: Instant? = null,
            terminatedAt: Instant? = null,
            error: String? = null,
        ): Delivery = Delivery(scheduledAt, status, attempts, lockedUntil, terminatedAt, error)
    }
}
