package pro.vlprojects.semantic.pattern.behavioral.resolver

internal fun checkHasNoBlankTypeName(groupsByTypeName: Map<String, List<*>>) {
    val blankNameElements = groupsByTypeName
        .filterKeys(String::isBlank)
        .values
        .flatten()
        .map { element -> "'$element'" }

    require(blankNameElements.isEmpty()) {
        "Type names are blank regardless of dashes and underscores: $blankNameElements"
    }
}

internal fun checkHasNoDuplicateTypeName(groupsByTypeName: Map<String, List<*>>) {
    val duplicateNameGroups = groupsByTypeName.filterValues { group -> group.size > 1 }

    require(duplicateNameGroups.isEmpty()) {
        "Duplicate type names regardless of case, dashes and underscores: $duplicateNameGroups"
    }
}

internal fun checkHasNoAnonymousClass(subtypes: Collection<Class<*>>) {
    val anonymousClasses = subtypes.filter(Class<*>::isAnonymousClass)

    require(anonymousClasses.isEmpty()) { "Anonymous classes have no type name: $anonymousClasses" }
}
