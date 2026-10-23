package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.ddd.support.toKebabCase

public val DomainObject.typeName: String
    get() = this::class.simpleName?.toKebabCase() ?: error("A domain object without a class name has no type name")
