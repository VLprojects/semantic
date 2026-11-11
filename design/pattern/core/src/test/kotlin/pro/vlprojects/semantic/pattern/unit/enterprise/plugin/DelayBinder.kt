package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.base.AbstractPolymorphicBinder
import kotlin.reflect.KClass

class DelayBinder(
    defaultTypeName: String = "exponential",
) : AbstractPolymorphicBinder<Any>(DelayTypeNameResolver, defaultTypeName) {

    override fun doBind(name: String, type: KClass<out Any>, sources: List<Map<String, String>>) =
        BoundDelay(name, type, sources)
}
