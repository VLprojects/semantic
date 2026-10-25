package pro.vlprojects.semantic.events

public class ConsumerId(public val value: String) {

    init {
        require(value.isNotBlank()) { "The id of a consumer must not be blank" }
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ConsumerId -> false
        else -> value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value
}
