package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import pro.vlprojects.semantic.pattern.enterprise.plugin.base.AbstractPolymorphicConfigurationBinder
import kotlin.reflect.KClass

class DelayBinder(
    defaultTypeName: String = "exponential",
    resolver: Resolver<String, KClass<out Delay>> = delayTypeNames,
) : AbstractPolymorphicConfigurationBinder<Delay>(resolver, defaultTypeName) {

    override fun doBind(name: String, type: KClass<out Delay>, sources: List<Map<String, String>>) = when (type) {
        FixedDelay::class -> FixedDelay(name, sources)
        ExponentialDelay::class -> ExponentialDelay(name, sources)
        else -> error("No delay is known of $type")
    }
}
