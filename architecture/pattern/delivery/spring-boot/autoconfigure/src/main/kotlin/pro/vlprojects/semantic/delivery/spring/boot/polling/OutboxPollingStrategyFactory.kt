package pro.vlprojects.semantic.delivery.spring.boot.polling

import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingStrategy

public interface OutboxPollingStrategyFactory<C : Any> {

    public val type: String

    public val configClass: Class<C>

    public fun create(config: C): OutboxPollingStrategy
}
