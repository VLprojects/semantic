package pro.vlprojects.semantic.delivery.outbox

public class OutboxId(public val value: String) {

    override fun equals(other: Any?): Boolean = other is OutboxId && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value
}
