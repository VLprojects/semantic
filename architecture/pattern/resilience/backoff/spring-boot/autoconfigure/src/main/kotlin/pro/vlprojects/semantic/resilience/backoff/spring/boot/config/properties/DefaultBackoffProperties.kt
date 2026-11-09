package pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.JitteredBackoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import java.time.Duration

public class DefaultBackoffProperties(
    public val type: BackoffType = BackoffType.EXPONENTIAL,
    public val delay: Duration = Duration.ofSeconds(1),
    public val initialDelay: Duration = Duration.ofSeconds(1),
    public val delayLimit: Duration = Duration.ofMinutes(1),
    public val multiplier: Double = 2.0,
    public val jitter: Double = JitteredBackoff.DEFAULT_JITTER,
) {

    internal fun createBackoff(instance: BackoffProperties = BackoffProperties()) = (instance.type ?: type)
        .also(instance::checkAllFieldsAreUsedBy)
        .let { resolvedType -> createUnjitteredBackoff(resolvedType, instance) }
        .jittered(instance.jitter ?: jitter)

    private fun createUnjitteredBackoff(resolvedType: BackoffType, instance: BackoffProperties): Backoff =
        when (resolvedType) {
            BackoffType.FIXED -> FixedBackoff(instance.delay ?: delay)

            BackoffType.EXPONENTIAL -> ExponentialBackoff(
                instance.initialDelay ?: initialDelay,
                instance.delayLimit ?: delayLimit,
                instance.multiplier ?: multiplier,
            )
        }
}
