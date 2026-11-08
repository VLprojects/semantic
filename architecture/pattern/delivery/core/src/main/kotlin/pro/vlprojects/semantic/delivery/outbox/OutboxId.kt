package pro.vlprojects.semantic.delivery.outbox

public class OutboxId private constructor(public val value: String) {

    override fun equals(other: Any?): Boolean = other is OutboxId && value == other.value

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value

    public companion object Factory {

        public fun of(value: String): OutboxId = OutboxId(value)
    }
}
