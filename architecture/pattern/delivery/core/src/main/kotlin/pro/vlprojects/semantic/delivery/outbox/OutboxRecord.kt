package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public class OutboxRecord(
    public val id: String,
    public val key: String,
    public val delivererId: String,
    public val payloadType: String,
    public val payload: String,
    public val idempotencyKey: String,
    public val createdAt: Instant,
    public val nextRetryAt: Instant,
    public val context: Map<String, String> = emptyMap(),
    public val status: OutboxRecordStatus = OutboxRecordStatus.NEW,
    public val failureCount: Int = 0,
    public val leaseToken: String? = null,
    public val leaseExpiresAt: Instant? = null,
    public val completedAt: Instant? = null,
    public val lastError: String? = null,
) {

    public val metadata: OutboxRecordMetadata
        get() = OutboxRecordMetadata(
            id = id,
            key = key,
            delivererId = delivererId,
            payloadType = payloadType,
            idempotencyKey = idempotencyKey,
            failureCount = failureCount,
            createdAt = createdAt,
            context = context,
        )
}
