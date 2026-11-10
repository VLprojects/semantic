package pro.vlprojects.semantic.delivery.spring.boot.polling

import pro.vlprojects.semantic.delivery.outbox.polling.ActiveIdlePollingStrategy
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy
import java.time.Duration

public class ActiveIdlePollingStrategyFactory :
    OutboxPollingStrategyFactory<ActiveIdlePollingStrategyFactory.Config> {

    override fun create(config: Config): OutboxPollingStrategy =
        ActiveIdlePollingStrategy.of(config.activeInterval, config.idleInterval)

    public data class Config(
        val activeInterval: Duration = Duration.ZERO,
        val idleInterval: Duration = Duration.ofSeconds(1),
    )
}
