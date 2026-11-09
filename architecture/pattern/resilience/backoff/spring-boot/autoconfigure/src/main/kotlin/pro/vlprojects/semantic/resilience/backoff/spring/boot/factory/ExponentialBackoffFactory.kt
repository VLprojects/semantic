package pro.vlprojects.semantic.resilience.backoff.spring.boot.factory

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import java.time.Duration

public const val EXPONENTIAL_BACKOFF_TYPE: String = "exponential"

public class ExponentialBackoffFactory : BackoffFactory<ExponentialBackoffFactory.Config> {

    override val type: String = EXPONENTIAL_BACKOFF_TYPE

    override val configClass: Class<Config> = Config::class.java

    override fun create(config: Config): Backoff =
        ExponentialBackoff(config.initialDelay, config.delayLimit, config.multiplier)

    public data class Config(
        val initialDelay: Duration = Duration.ofSeconds(1),
        val delayLimit: Duration = Duration.ofMinutes(1),
        val multiplier: Double = 2.0,
    )
}
