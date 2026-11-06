package pro.vlprojects.semantic.format

import kotlin.reflect.KClass

public fun KClass<*>.toKebabCase(): String = this.simpleName
    ?.toKebabCase()
    ?: error("$this has no class name to be named after")
