package pro.vlprojects.semantic.helper.text.predicate

public fun String.isAscii(): Boolean = this.all(Char::isAscii)

public fun String.isNotAscii(): Boolean = !this.isAscii()

public fun String.isLowercase(): Boolean = this.none(Char::isCapital)

public fun String.isUppercase(): Boolean = this.none(Char::isLowerCase)

public fun String.startsWithAny(vararg prefixes: String, ignoreCase: Boolean = false): Boolean = prefixes
    .any { this.startsWith(it, ignoreCase) }

public fun String.startsWithAny(prefixes: Iterable<String>, ignoreCase: Boolean = false): Boolean = prefixes
    .any { this.startsWith(it, ignoreCase) }

public fun String.endsWithAny(vararg suffixes: String, ignoreCase: Boolean = false): Boolean = suffixes
    .any { this.endsWith(it, ignoreCase) }

public fun String.endsWithAny(suffixes: Iterable<String>, ignoreCase: Boolean = false): Boolean = suffixes
    .any { this.endsWith(it, ignoreCase) }

public fun String.notStartsWithAny(vararg prefixes: String, ignoreCase: Boolean = false): Boolean =
    !this.startsWithAny(*prefixes, ignoreCase = ignoreCase)

public fun String.notStartsWithAny(prefixes: Iterable<String>, ignoreCase: Boolean = false): Boolean =
    !this.startsWithAny(prefixes, ignoreCase)

public fun String.notEndsWithAny(vararg suffixes: String, ignoreCase: Boolean = false): Boolean =
    !this.endsWithAny(*suffixes, ignoreCase = ignoreCase)

public fun String.notEndsWithAny(suffixes: Iterable<String>, ignoreCase: Boolean = false): Boolean =
    !this.endsWithAny(suffixes, ignoreCase)
