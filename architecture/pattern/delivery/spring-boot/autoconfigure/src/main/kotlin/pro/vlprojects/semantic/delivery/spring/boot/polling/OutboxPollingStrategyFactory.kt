package pro.vlprojects.semantic.delivery.spring.boot.polling

import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy

public interface OutboxPollingStrategyFactory<C : Any> {

    public fun create(config: C): OutboxPollingStrategy
}
