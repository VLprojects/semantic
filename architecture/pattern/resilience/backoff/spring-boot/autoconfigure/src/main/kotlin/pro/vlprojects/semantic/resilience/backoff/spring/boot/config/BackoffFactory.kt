package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffType
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.DefaultBackoffProperties

internal class BackoffFactory(private val defaults: DefaultBackoffProperties) {

    fun create(instance: BackoffProperties) = (instance.type ?: defaults.type)
        .also { type -> rejectFieldsOfOtherType(type, instance) }
        .let { type -> createUnjitteredBackoff(type, instance) }
        .jittered(instance.jitter ?: defaults.jitter)

    private fun rejectFieldsOfOtherType(type: BackoffType, instance: BackoffProperties) =
        findFieldsOfOtherType(type, instance)
            .filterValues { value -> value != null }
            .keys
            .takeIf(Set<String>::isNotEmpty)
            ?.let { fields -> throw IllegalArgumentException("The type $type does not use $fields") }

    private fun findFieldsOfOtherType(type: BackoffType, instance: BackoffProperties) = when (type) {
        BackoffType.FIXED -> mapOf(
            "initial-delay" to instance.initialDelay,
            "delay-limit" to instance.delayLimit,
            "multiplier" to instance.multiplier,
        )

        BackoffType.EXPONENTIAL -> mapOf("delay" to instance.delay)
    }

    private fun createUnjitteredBackoff(type: BackoffType, instance: BackoffProperties) = when (type) {
        BackoffType.FIXED -> FixedBackoff(instance.delay ?: defaults.delay)

        BackoffType.EXPONENTIAL -> ExponentialBackoff(
            instance.initialDelay ?: defaults.initialDelay,
            instance.delayLimit ?: defaults.delayLimit,
            instance.multiplier ?: defaults.multiplier,
        )
    }
}
