package pro.vlprojects.semantic.delivery.outbox

import pro.vlprojects.semantic.format.abbreviation.SuffixAbbreviation
import pro.vlprojects.semantic.format.abbreviation.abbreviated
import java.time.Instant

public data class Delivery(
    public val scheduledAt: Instant,
    public val status: DeliveryStatus = DeliveryStatus.AWAITING,
    public val attempts: Int = 0,
    public val lockedUntil: Instant? = null,
    public val completedAt: Instant? = null,
    public val error: String? = null,
) {

    internal fun started(lockedUntil: Instant): Delivery {
        checkHasNotTerminalStatus()
        return copy(status = DeliveryStatus.IN_PROGRESS, attempts = attempts + 1, lockedUntil = lockedUntil)
    }

    internal fun completed(completedAt: Instant): Delivery {
        checkHasInProgressStatus()
        return copy(status = DeliveryStatus.COMPLETED, lockedUntil = null, completedAt = completedAt, error = null)
    }

    internal fun failed(scheduledAt: Instant, error: String): Delivery {
        checkHasInProgressStatus()
        val abbreviatedError = abbreviate(error)
        return copy(
            scheduledAt = scheduledAt,
            status = DeliveryStatus.FAILED,
            lockedUntil = null,
            error = abbreviatedError,
        )
    }

    internal fun canceled(error: String): Delivery {
        checkHasInProgressStatus()
        val abbreviatedError = abbreviate(error)
        return copy(status = DeliveryStatus.CANCELED, lockedUntil = null, error = abbreviatedError)
    }

    private fun checkHasNotTerminalStatus() = check(!status.isTerminal) { "Delivery is already $status" }

    private fun checkHasInProgressStatus() = check(status == DeliveryStatus.IN_PROGRESS) { "Delivery is $status" }

    private fun abbreviate(error: String) = error.abbreviated(
        minLength = MAX_ERROR_LENGTH,
        visibleLength = MAX_ERROR_LENGTH - 1,
        abbreviation = SuffixAbbreviation,
    )

    private companion object {

        const val MAX_ERROR_LENGTH = 1000
    }
}
