package pro.vlprojects.semantic.delivery.outbox

public class OutboxPayload(
    public val className: String,
    public val content: ByteArray,
) {

    override fun equals(other: Any?): Boolean =
        other is OutboxPayload && className == other.className && content.contentEquals(other.content)

    override fun hashCode(): Int = content.contentHashCode().let { 31 * it + className.hashCode() }
}
