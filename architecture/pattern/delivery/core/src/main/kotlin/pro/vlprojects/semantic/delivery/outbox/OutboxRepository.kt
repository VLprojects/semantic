package pro.vlprojects.semantic.delivery.outbox

import java.time.Duration
import java.time.Instant

public interface OutboxRepository {

    public fun save(outbox: Outbox)

    public fun claim(limit: Int, lease: Duration, now: Instant): List<Outbox>

    public fun complete(id: String, token: String, now: Instant): Boolean

    public fun reschedule(id: String, token: String, failureCount: Int, nextRetryAt: Instant, error: String): Boolean

    public fun fail(id: String, token: String, error: String): Boolean

    public fun purge(completedBefore: Instant, limit: Int): Int
}
