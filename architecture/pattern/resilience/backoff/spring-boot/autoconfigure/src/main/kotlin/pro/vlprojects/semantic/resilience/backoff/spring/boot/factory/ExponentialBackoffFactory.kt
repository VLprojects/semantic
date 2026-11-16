package pro.vlprojects.semantic.resilience.backoff.spring.boot.factory

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.ExponentialBackoff
import pro.vlprojects.semantic.resilience.backoff.extension.jittered
import java.time.Duration

private const val NO_JITTER = 0.0

public class ExponentialBackoffFactory : BackoffFactory<ExponentialBackoffFactory.Config> {

    override fun create(config: Config): Backoff {
        val backoff = ExponentialBackoff(config.initialDelay, config.delayLimit, config.multiplier)

        return when (config.jitter) {
            NO_JITTER -> backoff
            else -> backoff.jittered(config.jitter)
        }
    }

    public data class Config(
        val initialDelay: Duration = Duration.ofSeconds(1),
        val delayLimit: Duration = Duration.ofMinutes(1),
        val multiplier: Double = 2.0,
        val jitter: Double = NO_JITTER,
    )
}
