package pro.vlprojects.semantic.resilience.backoff.spring.boot

import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.JitteredBackoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import java.time.Duration

internal class ResolvedBackoffProperties(
    private val type: BackoffType = BackoffType.EXPONENTIAL,
    private val delay: Duration = Duration.ofSeconds(1),
    private val initialDelay: Duration = Duration.ofSeconds(1),
    private val delayLimit: Duration = Duration.ofMinutes(1),
    private val multiplier: Double = 2.0,
    private val jitter: Double = JitteredBackoff.DEFAULT_JITTER,
) {

    fun overrideWith(properties: BackoffInstanceProperties) = ResolvedBackoffProperties(
        type = properties.type ?: type,
        delay = properties.delay ?: delay,
        initialDelay = properties.initialDelay ?: initialDelay,
        delayLimit = properties.delayLimit ?: delayLimit,
        multiplier = properties.multiplier ?: multiplier,
        jitter = properties.jitter ?: jitter,
    )

    fun checkAllFieldsAreUsed(properties: BackoffInstanceProperties) = properties
        .findFieldsUnusedBy(type)
        .takeIf(List<String>::isNotEmpty)
        ?.let { unused -> throw IllegalArgumentException("The type $type does not use $unused") }

    fun createBackoff() = this
        .createUnjitteredBackoff()
        .jittered(jitter)

    private fun createUnjitteredBackoff() = when (type) {
        BackoffType.FIXED -> FixedBackoff(delay)
        BackoffType.EXPONENTIAL -> ExponentialBackoff(initialDelay, delayLimit, multiplier)
    }
}
