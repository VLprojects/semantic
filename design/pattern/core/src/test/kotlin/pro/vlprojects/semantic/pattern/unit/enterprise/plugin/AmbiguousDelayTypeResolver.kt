package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import kotlin.reflect.KClass

val ambiguousDelayTypeResolver = Resolver<String, KClass<out Delay>> { typeName ->
    when (typeName) {
        "fixed" -> listOf(FixedDelay::class, ExponentialDelay::class)
        else -> listOf(ExponentialDelay::class)
    }
}
