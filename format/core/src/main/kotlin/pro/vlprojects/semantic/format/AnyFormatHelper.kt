package pro.vlprojects.semantic.format

public val Any.kebabClassName: String
    get() = this::class.simpleName
        ?.toKebabCase()
        ?: error("${this::class} has no class name to be named after")
