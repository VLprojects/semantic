package pro.vlprojects.semantic.pattern.enterprise.plugin

import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationPropertyException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleConfigurationsException

internal fun checkHasNoMultipleTypes(prefix: String, typeNamesByType: Map<*, String>) {
    if (typeNamesByType.size > 1) {
        val typeNames = typeNamesByType.values.toSet()

        throw MultipleConfigurationsException(prefix, typeNames)
    }
}

internal fun checkHasNoUnboundProperties(prefix: String, properties: Map<String, String>) {
    val unboundKeys = properties
        .filter(Map.Entry<String, String>::isUnbound)
        .keys
        .map { key -> prefix.appendKey(key) }
        .map { key -> "'$key'" }

    if (unboundKeys.isNotEmpty()) {
        val cause = IllegalArgumentException("Unbound configuration properties: $unboundKeys")

        throw InvalidConfigurationPropertyException(prefix, cause)
    }
}
