package pro.vlprojects.semantic.format

public fun Int.pluralize(one: String, few: String, many: String): String = this
    .toLong()
    .pluralize(one, few, many)
