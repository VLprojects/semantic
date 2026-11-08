package pro.vlprojects.semantic.delivery.outbox

public interface OutboxAdministration {

    public fun count(status: OutboxStatus): Long

    public fun retryFailed(key: String?): Int

    public fun delete(status: OutboxStatus, key: String?): Int
}
