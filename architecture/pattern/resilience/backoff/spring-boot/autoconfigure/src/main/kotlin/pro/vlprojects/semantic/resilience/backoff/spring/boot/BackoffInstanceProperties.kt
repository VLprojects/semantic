package pro.vlprojects.semantic.resilience.backoff.spring.boot

import java.time.Duration

public class BackoffInstanceProperties(
    public val type: BackoffType? = null,
    public val delay: Duration? = null,
    public val initialDelay: Duration? = null,
    public val delayLimit: Duration? = null,
    public val multiplier: Double? = null,
    public val jitter: Double? = null,
) {

    internal fun findFieldsUnusedBy(type: BackoffType) = when (type) {
        BackoffType.FIXED -> listOfNotNull(
            initialDelay?.let { "initial-delay" },
            delayLimit?.let { "delay-limit" },
            multiplier?.let { "multiplier" },
        )

        BackoffType.EXPONENTIAL -> listOfNotNull(delay?.let { "delay" })
    }
}
