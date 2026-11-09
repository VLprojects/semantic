package pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties

import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.EXPONENTIAL_BACKOFF_TYPE

public data class BackoffDefaultProperties(
    val type: String = EXPONENTIAL_BACKOFF_TYPE,
    val args: Map<String, String> = emptyMap(),
    val jitter: Double? = null,
)
