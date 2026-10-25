package pro.vlprojects.semantic.ddd.support

import pro.vlprojects.semantic.format.toKebabCase

internal val Any.kebabClassName
    get() = this::class.simpleName
        ?.toKebabCase()
        ?: error("${this::class} has no class name to be named after")
