package pro.vlprojects.semantic.delivery.outbox

import kotlin.reflect.KClass

public interface PayloadDeserializer {

    public fun <T : Any> deserialize(body: ByteArray, type: KClass<T>): T
}
