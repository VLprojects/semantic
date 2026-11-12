package pro.vlprojects.semantic.pattern.enterprise.configuration

import java.util.Objects

public class Definition private constructor(
    public val name: String,
    public val type: String,
    arguments: Map<String, String>,
) {

    public val arguments: Map<String, String> = arguments.toMap()

    init {
        require(name.isNotBlank()) { "The name of a configuration definition is blank" }
        require(type.isNotBlank()) { "The type of the configuration definition '$name' is blank" }
        require(arguments.keys.none(String::isBlank)) {
            "An argument of the configuration definition '$name' has a blank key"
        }
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Definition -> false
        else -> name == other.name && type == other.type && arguments == other.arguments
    }

    override fun hashCode(): Int = Objects.hash(name, type, arguments)

    override fun toString(): String = "[name: $name, type: $type, arguments: ${arguments.keys}]"

    public companion object Factory {

        public fun of(
            name: String,
            type: String,
            arguments: Map<String, String> = emptyMap(),
        ): Definition = Definition(name, type, arguments)
    }
}
