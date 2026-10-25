package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.helper.format.toKebabCase
import kotlin.reflect.KClass

public val DomainObject.typeName: String
    get() = this::class.typeName

public val KClass<out DomainObject>.typeName: String
    get() = simpleName?.toKebabCase()
        ?: error("A domain object without a class name has no type name")
