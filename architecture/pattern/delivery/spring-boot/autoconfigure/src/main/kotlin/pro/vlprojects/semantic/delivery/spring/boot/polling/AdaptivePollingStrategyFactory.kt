package pro.vlprojects.semantic.delivery.spring.boot.polling

import pro.vlprojects.semantic.delivery.outbox.polling.AdaptivePollingStrategy
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy
import java.time.Duration

public class AdaptivePollingStrategyFactory : OutboxPollingStrategyFactory<AdaptivePollingStrategyFactory.Config> {

    override fun create(config: Config): OutboxPollingStrategy =
        AdaptivePollingStrategy.of(config.minInterval, config.maxInterval, config.multiplier)

    public data class Config(
        val minInterval: Duration = Duration.ofMillis(100),
        val maxInterval: Duration = Duration.ofSeconds(10),
        val multiplier: Double = 2.0,
    )
}
