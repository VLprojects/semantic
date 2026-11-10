package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public data class AdaptivePollingProperties(
    public val minInterval: Duration = Duration.ofMillis(100),
    public val maxInterval: Duration = Duration.ofSeconds(10),
)
