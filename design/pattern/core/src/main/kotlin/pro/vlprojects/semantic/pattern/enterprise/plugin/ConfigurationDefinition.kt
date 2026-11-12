package pro.vlprojects.semantic.pattern.enterprise.plugin

import java.util.Objects

public class ConfigurationDefinition private constructor(
    public val name: String,
    public val typeName: String?,
    public val arguments: Map<String, String>,
) {

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ConfigurationDefinition -> false
        else -> name == other.name && typeName == other.typeName && arguments == other.arguments
    }

    override fun hashCode(): Int = Objects.hash(name, typeName, arguments)

    override fun toString(): String = "[name: $name, typeName: $typeName, arguments: ${arguments.keys}]"

    public companion object Factory {

        public fun of(
            name: String,
            typeName: String? = null,
            arguments: Map<String, String> = emptyMap(),
        ): ConfigurationDefinition = ConfigurationDefinition(name, typeName, arguments)
    }
}
