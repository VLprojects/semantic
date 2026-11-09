package pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties

import java.time.Duration

public data class BackoffProperties(
    val type: BackoffType? = null,
    val delay: Duration? = null,
    val initialDelay: Duration? = null,
    val delayLimit: Duration? = null,
    val multiplier: Double? = null,
    val jitter: Double? = null,
)
