package pro.vlprojects.semantic.pattern.enterprise.plugin.base

import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleTypesException

internal fun checkHasDefaultType(resolver: Resolver<String, *>, defaultTypeName: String) {
    val defaultType = resolver.resolveOrNull(defaultTypeName)

    requireNotNull(defaultType) { "The default type '$defaultTypeName' is unknown to the resolver" }
}

internal fun checkHasSingleType(name: String, typeNames: Collection<String>) {
    if (typeNames.size > 1) {
        throw MultipleTypesException(name, typeNames.toSet())
    }
}
