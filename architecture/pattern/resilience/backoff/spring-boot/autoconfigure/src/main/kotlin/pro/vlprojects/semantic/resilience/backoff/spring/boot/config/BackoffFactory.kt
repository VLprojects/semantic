package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffDefaultProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffType

internal class BackoffFactory(private val defaults: BackoffDefaultProperties) {

    fun create(properties: BackoffProperties) = when (properties.type ?: defaults.type) {
        BackoffType.FIXED -> createFixedBackoff(properties)
        BackoffType.EXPONENTIAL -> createExponentialBackoff(properties)
    }.let { backoff -> applyJitter(backoff, properties) }

    private fun createFixedBackoff(properties: BackoffProperties) = properties
        .also(::validateFixedBackoff)
        .let { FixedBackoff(properties.delay ?: defaults.delay) }

    private fun createExponentialBackoff(properties: BackoffProperties) = properties
        .also(::validateExponentialBackoff)
        .let {
            ExponentialBackoff(
                properties.initialDelay ?: defaults.initialDelay,
                properties.delayLimit ?: defaults.delayLimit,
                properties.multiplier ?: defaults.multiplier,
            )
        }

    private fun validateFixedBackoff(properties: BackoffProperties) =
        listOfNotNull(properties.initialDelay, properties.delayLimit, properties.multiplier)
            .isEmpty()
            .let { isValid ->
                require(isValid) { "A fixed backoff does not use initial-delay, delay-limit or multiplier" }
            }

    private fun validateExponentialBackoff(properties: BackoffProperties) =
        require(properties.delay == null) { "An exponential backoff does not use delay" }

    private fun applyJitter(backoff: Backoff, properties: BackoffProperties) = (properties.jitter ?: defaults.jitter)
        ?.let { jitter -> backoff.jittered(jitter) }
        ?: backoff
}
