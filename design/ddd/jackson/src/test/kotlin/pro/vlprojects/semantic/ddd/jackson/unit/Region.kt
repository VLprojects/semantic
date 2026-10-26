package pro.vlprojects.semantic.ddd.jackson.unit

import pro.vlprojects.semantic.ddd.tactical.BoxedIdentifier

enum class Region(override val value: String) : BoxedIdentifier<String> {

    EUROPE("eu"),
    ASIA("as"),
}
