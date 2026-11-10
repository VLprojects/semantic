package pro.vlprojects.semantic.delivery.outbox

import kotlin.reflect.KClass

public class Payload private constructor(
    public val type: String,
    public val body: ByteArray,
) {

    override fun equals(other: Any?): Boolean =
        other is Payload && type == other.type && body.contentEquals(other.body)

    override fun hashCode(): Int = body.contentHashCode().let { bodyHash -> 31 * bodyHash + type.hashCode() }

    public companion object Factory {

        public inline fun <reified T : Any> of(body: ByteArray): Payload = of(T::class, body)

        public fun of(type: KClass<*>, body: ByteArray): Payload = of(type.javaObjectType.name, body)

        public fun of(type: String, body: ByteArray): Payload = Payload(type, body)
    }
}
