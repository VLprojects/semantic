package pro.vlprojects.semantic.delivery.outbox

public class Payload(
    public val className: String,
    public val content: ByteArray,
) {

    override fun equals(other: Any?): Boolean =
        other is Payload && className == other.className && content.contentEquals(other.content)

    override fun hashCode(): Int = content.contentHashCode().let { 31 * it + className.hashCode() }
}
