package pro.vlprojects.semantic.delivery.spring.boot.polling

import pro.vlprojects.semantic.delivery.outbox.polling.FixedPollingStrategy
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy
import java.time.Duration

public const val FIXED_POLLING_STRATEGY_TYPE: String = "fixed"

public class FixedPollingStrategyFactory : OutboxPollingStrategyFactory<FixedPollingStrategyFactory.Config> {

    override val type: String = FIXED_POLLING_STRATEGY_TYPE

    override val configClass: Class<Config> = Config::class.java

    override fun create(config: Config): OutboxPollingStrategy = FixedPollingStrategy.of(config.interval)

    public data class Config(val interval: Duration = Duration.ofSeconds(1))
}
