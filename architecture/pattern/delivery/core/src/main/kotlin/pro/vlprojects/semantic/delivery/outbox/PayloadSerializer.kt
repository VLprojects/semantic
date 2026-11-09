package pro.vlprojects.semantic.delivery.outbox

import kotlin.reflect.KClass

public interface PayloadSerializer {

    public fun serialize(payload: Any): ByteArray

    public fun <T : Any> deserialize(content: ByteArray, type: KClass<T>): T
}
