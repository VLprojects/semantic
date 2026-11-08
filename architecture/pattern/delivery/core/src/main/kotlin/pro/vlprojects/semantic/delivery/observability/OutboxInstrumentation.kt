package pro.vlprojects.semantic.delivery.observability

import pro.vlprojects.semantic.delivery.outbox.OutboxRecordMetadata
import java.time.Duration

public interface OutboxInstrumentation {

    public fun onScheduled(record: OutboxRecordMetadata): Unit = Unit

    public fun onHandlingStarted(record: OutboxRecordMetadata): Unit = Unit

    public fun onHandled(record: OutboxRecordMetadata, duration: Duration): Unit = Unit

    public fun onFailed(record: OutboxRecordMetadata, duration: Duration, failure: Throwable): Unit = Unit
}
