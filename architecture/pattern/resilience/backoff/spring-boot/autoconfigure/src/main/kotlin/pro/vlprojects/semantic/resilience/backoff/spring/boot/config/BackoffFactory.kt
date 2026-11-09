package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffDefaultProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffType

internal class BackoffFactory(private val defaults: BackoffDefaultProperties) {

    fun create(properties: BackoffProperties): Backoff {
        val backoff = when (properties.type ?: defaults.type) {
            BackoffType.FIXED -> createFixedBackoff(properties)
            BackoffType.EXPONENTIAL -> createExponentialBackoff(properties)
        }
        val jitter = properties.jitter ?: defaults.jitter

        return if (jitter == null) backoff else backoff.jittered(jitter)
    }

    private fun createFixedBackoff(properties: BackoffProperties): Backoff {
        require(properties.initialDelay == null) { "A fixed backoff does not use initial-delay" }
        require(properties.delayLimit == null) { "A fixed backoff does not use delay-limit" }
        require(properties.multiplier == null) { "A fixed backoff does not use multiplier" }

        return FixedBackoff(properties.delay ?: defaults.delay)
    }

    private fun createExponentialBackoff(properties: BackoffProperties): Backoff {
        require(properties.delay == null) { "An exponential backoff does not use delay" }

        return ExponentialBackoff(
            properties.initialDelay ?: defaults.initialDelay,
            properties.delayLimit ?: defaults.delayLimit,
            properties.multiplier ?: defaults.multiplier,
        )
    }
}
