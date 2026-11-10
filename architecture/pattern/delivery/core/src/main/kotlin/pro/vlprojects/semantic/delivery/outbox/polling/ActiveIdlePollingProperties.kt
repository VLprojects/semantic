package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public data class ActiveIdlePollingProperties(
    public val activeInterval: Duration = Duration.ZERO,
    public val idleInterval: Duration = Duration.ofSeconds(1),
)
