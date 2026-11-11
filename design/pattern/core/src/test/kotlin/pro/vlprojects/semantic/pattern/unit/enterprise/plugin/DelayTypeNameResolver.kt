package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.TypeNameResolver

object DelayTypeNameResolver : TypeNameResolver<Any> {

    private val types = mapOf(
        "exponential" to ExponentialDelay::class,
        "fixed" to FixedDelay::class,
    )

    override val typeNames = types.keys

    override fun find(typeName: String) = typeName
        .lowercase()
        .let(types::get)
}
