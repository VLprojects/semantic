package pro.vlprojects.semantic.ddd.jackson

import pro.vlprojects.semantic.ddd.tactical.BoxedIdentifier
import tools.jackson.databind.BeanDescription
import tools.jackson.databind.DeserializationConfig
import tools.jackson.databind.JavaType
import tools.jackson.databind.deser.KeyDeserializers

internal object BoxedIdentifierKeyDeserializerFinder : KeyDeserializers {

    // An enum is left to Jackson, which reads its key back through the value the enum writes.
    override fun findKeyDeserializer(
        type: JavaType,
        config: DeserializationConfig,
        beanDescriptionSupplier: BeanDescription.Supplier,
    ) = when {
        type.isEnumType -> null
        type.isTypeOrSubTypeOf(BoxedIdentifier::class.java) -> BoxedIdentifierKeyDeserializer(type)
        else -> null
    }
}
