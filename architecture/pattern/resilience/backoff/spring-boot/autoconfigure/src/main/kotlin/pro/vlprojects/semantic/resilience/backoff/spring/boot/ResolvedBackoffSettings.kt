package pro.vlprojects.semantic.resilience.backoff.spring.boot

import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.JitteredBackoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import java.time.Duration

internal class ResolvedBackoffSettings(
    private val type: BackoffType = BackoffType.EXPONENTIAL,
    private val delay: Duration = Duration.ofSeconds(1),
    private val initialDelay: Duration = Duration.ofSeconds(1),
    private val delayLimit: Duration = Duration.ofMinutes(1),
    private val multiplier: Double = 2.0,
    private val jitter: Double = JitteredBackoff.DEFAULT_JITTER,
) {

    fun overrideWith(settings: BackoffSettings) = ResolvedBackoffSettings(
        type = settings.type ?: type,
        delay = settings.delay ?: delay,
        initialDelay = settings.initialDelay ?: initialDelay,
        delayLimit = settings.delayLimit ?: delayLimit,
        multiplier = settings.multiplier ?: multiplier,
        jitter = settings.jitter ?: jitter,
    )

    fun checkAllFieldsAreUsed(settings: BackoffSettings) = settings
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
