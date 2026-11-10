package pro.vlprojects.semantic.delivery.spring.boot.polling

import pro.vlprojects.semantic.delivery.outbox.polling.AdaptivePollingStrategy
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy
import java.time.Duration

public const val ADAPTIVE_POLLING_STRATEGY_TYPE: String = "adaptive"

public class AdaptivePollingStrategyFactory : OutboxPollingStrategyFactory<AdaptivePollingStrategyFactory.Config> {

    override val type: String = ADAPTIVE_POLLING_STRATEGY_TYPE

    override val configClass: Class<Config> = Config::class.java

    override fun create(config: Config): OutboxPollingStrategy =
        AdaptivePollingStrategy.of(config.minInterval, config.maxInterval, config.multiplier)

    public data class Config(
        val minInterval: Duration = Duration.ofMillis(100),
        val maxInterval: Duration = Duration.ofSeconds(10),
        val multiplier: Double = 2.0,
    )
}
