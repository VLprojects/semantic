package pro.vlprojects.semantic.delivery.outbox

public interface OutboxRecordAdministration {

    public fun count(status: OutboxRecordStatus): Long

    public fun retryFailed(key: String?): Int

    public fun delete(status: OutboxRecordStatus, key: String?): Int
}
