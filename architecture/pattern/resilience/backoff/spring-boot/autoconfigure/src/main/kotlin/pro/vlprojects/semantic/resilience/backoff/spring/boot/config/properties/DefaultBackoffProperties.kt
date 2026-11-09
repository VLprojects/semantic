package pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties

import pro.vlprojects.semantic.resilience.backoff.JitteredBackoff
import java.time.Duration

public data class DefaultBackoffProperties(
    val type: BackoffType = BackoffType.EXPONENTIAL,
    val delay: Duration = Duration.ofSeconds(1),
    val initialDelay: Duration = Duration.ofSeconds(1),
    val delayLimit: Duration = Duration.ofMinutes(1),
    val multiplier: Double = 2.0,
    val jitter: Double = JitteredBackoff.DEFAULT_JITTER,
)
