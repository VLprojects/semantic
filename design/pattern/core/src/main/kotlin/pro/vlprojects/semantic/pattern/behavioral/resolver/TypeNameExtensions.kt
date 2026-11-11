package pro.vlprojects.semantic.pattern.behavioral.resolver

import kotlin.reflect.KClass

private val NAME_SEPARATORS = setOf('-', '_')

internal fun KClass<*>.buildTypeName(baseType: KClass<*>) = this.java.simpleName
    .removeSuffix(baseType.java.simpleName)
    .ifEmpty { this.java.simpleName }

internal fun String.toUniformForm() = this
    .lowercase()
    .filterNot(NAME_SEPARATORS::contains)

private fun String.isBlankName() = this
    .toUniformForm()
    .isBlank()

private fun <T, K> Iterable<T>.findDuplicatesBy(keySelector: (T) -> K) = this
    .groupBy(keySelector)
    .values
    .filter { duplicates -> duplicates.size > 1 }

internal fun <T : Any> Map<String, KClass<out T>>.requireNotBlankTypeNames(): Map<String, KClass<out T>> {
    val blankNames = this.keys
        .filter(String::isBlankName)
        .map { typeName -> "'$typeName'" }

    require(blankNames.isEmpty()) { "Type names are blank regardless of dashes and underscores: $blankNames" }

    return this
}

internal fun <T : Any> Map<String, KClass<out T>>.requireDistinctTypeNames(): Map<String, KClass<out T>> {
    val coincidingNames = this.keys.findDuplicatesBy(String::toUniformForm)

    require(coincidingNames.isEmpty()) {
        "Type names coincide regardless of case, dashes and underscores: $coincidingNames"
    }

    return this
}

internal fun <T : Any> Set<KClass<out T>>.requireNamedClasses(): Set<KClass<out T>> {
    val anonymousClassNames = this
        .map(KClass<*>::java)
        .filter(Class<*>::isAnonymousClass)
        .map(Class<*>::getName)

    require(anonymousClassNames.isEmpty()) { "Anonymous classes have no type name: $anonymousClassNames" }

    return this
}

internal fun <T : Any> Set<KClass<out T>>.requireDistinctTypeNames(baseType: KClass<T>): Set<KClass<out T>> {
    val coincidingClassNames = this
        .findDuplicatesBy { subtype -> subtype.buildTypeName(baseType).toUniformForm() }
        .map { duplicates ->
            duplicates
                .map(KClass<*>::java)
                .map(Class<*>::getName)
        }

    require(coincidingClassNames.isEmpty()) {
        "Subtype names coincide regardless of case, dashes and underscores: $coincidingClassNames"
    }

    return this
}
