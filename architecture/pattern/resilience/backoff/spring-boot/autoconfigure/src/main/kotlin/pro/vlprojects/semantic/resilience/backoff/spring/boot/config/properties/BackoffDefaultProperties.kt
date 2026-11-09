package pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties

import java.time.Duration

public data class BackoffDefaultProperties(
    val type: BackoffType = BackoffType.EXPONENTIAL,
    val delay: Duration = Duration.ofSeconds(1),
    val initialDelay: Duration = Duration.ofSeconds(1),
    val delayLimit: Duration = Duration.ofMinutes(1),
    val multiplier: Double = 2.0,
    val jitter: Double? = null,
)
