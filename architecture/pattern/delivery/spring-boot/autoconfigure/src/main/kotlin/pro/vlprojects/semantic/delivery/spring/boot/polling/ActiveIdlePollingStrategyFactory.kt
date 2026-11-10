package pro.vlprojects.semantic.delivery.spring.boot.polling

import pro.vlprojects.semantic.delivery.outbox.polling.ActiveIdlePollingStrategy
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy
import java.time.Duration

public const val ACTIVE_IDLE_POLLING_STRATEGY_TYPE: String = "active-idle"

public class ActiveIdlePollingStrategyFactory :
    OutboxPollingStrategyFactory<ActiveIdlePollingStrategyFactory.Config> {

    override val type: String = ACTIVE_IDLE_POLLING_STRATEGY_TYPE

    override val configClass: Class<Config> = Config::class.java

    override fun create(config: Config): OutboxPollingStrategy =
        ActiveIdlePollingStrategy.of(config.activeInterval, config.idleInterval)

    public data class Config(
        val activeInterval: Duration = Duration.ZERO,
        val idleInterval: Duration = Duration.ofSeconds(1),
    )
}
