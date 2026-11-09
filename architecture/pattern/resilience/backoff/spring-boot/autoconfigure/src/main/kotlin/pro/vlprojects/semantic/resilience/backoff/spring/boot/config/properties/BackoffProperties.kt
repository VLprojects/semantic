package pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties

public data class BackoffProperties(
    val type: String? = null,
    val args: Map<String, String> = emptyMap(),
    val jitter: Double? = null,
)
