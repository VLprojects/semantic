package pro.vlprojects.semantic.delivery.outbox

import java.time.Instant

public class OutboxRecordMetadata(
    public val id: String,
    public val key: String,
    public val handlerId: String,
    public val payloadType: String,
    public val idempotencyKey: String,
    public val failureCount: Int,
    public val createdAt: Instant,
    public val context: Map<String, String>,
)
