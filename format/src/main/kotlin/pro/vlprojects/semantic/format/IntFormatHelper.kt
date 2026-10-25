package pro.vlprojects.semantic.format

public fun Int.pluralize(one: String, few: String, many: String): String = this
    .toLong()
    .pluralize(one, few, many)

public fun Int.pluralizeWithCount(one: String, few: String, many: String): String = this
    .toLong()
    .pluralizeWithCount(one, few, many)
