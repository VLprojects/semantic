package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.format.abbreviation.SuffixAbbreviation
import pro.vlprojects.semantic.format.abbreviation.abbreviated
import java.time.Instant

public data class Delivery(
    public val scheduledAt: Instant,
    public val status: DeliveryStatus = DeliveryStatus.PENDING,
    public val attempts: Int = 0,
    public val lockedUntil: Instant? = null,
    public val terminatedAt: Instant? = null,
    public val error: String? = null,
) {

    internal fun started(lockedUntil: Instant): Delivery {
        checkHasNotTerminalStatus()
        return copy(status = DeliveryStatus.STARTED, attempts = attempts + 1, lockedUntil = lockedUntil)
    }

    internal fun completed(terminatedAt: Instant): Delivery {
        checkHasStartedStatus()
        return copy(status = DeliveryStatus.COMPLETED, lockedUntil = null, terminatedAt = terminatedAt, error = null)
    }

    internal fun failed(scheduledAt: Instant, cause: Throwable): Delivery {
        checkHasStartedStatus()
        val abbreviatedError = cause.toString().abbreviated(
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

    private companion object {

        const val MAX_ERROR_LENGTH = 1000
    }
}
