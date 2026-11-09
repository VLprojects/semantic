package pro.vlprojects.semantic.delivery.outbox

import kotlin.reflect.KClass

public class Payload private constructor(
    public val type: String,
    public val content: ByteArray,
) {

    override fun equals(other: Any?): Boolean =
        other is Payload && type == other.type && content.contentEquals(other.content)

    override fun hashCode(): Int = content.contentHashCode().let { 31 * it + type.hashCode() }

    public companion object Factory {

        public inline fun <reified T : Any> of(content: ByteArray): Payload = of(T::class, content)

        public fun of(type: KClass<*>, content: ByteArray): Payload = of(type.javaObjectType.name, content)

        public fun of(type: String, content: ByteArray): Payload = Payload(type, content)
    }
}
