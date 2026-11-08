package pro.vlprojects.semantic.delivery.observability

import pro.vlprojects.semantic.delivery.outbox.OutboxMetadata
import java.time.Duration

public interface OutboxInstrumentation {

    public fun onScheduled(record: OutboxMetadata): Unit = Unit

    public fun onHandlingStarted(record: OutboxMetadata): Unit = Unit

    public fun onHandled(record: OutboxMetadata, duration: Duration): Unit = Unit

    public fun onFailed(record: OutboxMetadata, duration: Duration, failure: Throwable): Unit = Unit
}
