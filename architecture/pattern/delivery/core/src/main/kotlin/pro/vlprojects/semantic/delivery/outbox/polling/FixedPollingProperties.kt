package pro.vlprojects.semantic.delivery.outbox.polling

import java.time.Duration

public data class FixedPollingProperties(public val interval: Duration = Duration.ofSeconds(1))
