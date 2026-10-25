package pro.vlprojects.semantic.helper.text.predicate

public fun String.isAscii(): Boolean = this.all(Char::isAscii)

public fun String.isNotAscii(): Boolean = !this.isAscii()

public fun String.isLowercase(): Boolean = this.none(Char::isCapital)

public fun String.startsWithAny(vararg prefixes: String): Boolean = prefixes.any(::startsWith)

public fun String.startsWithAny(prefixes: Iterable<String>): Boolean = prefixes.any(::startsWith)

public fun String.endsWithAny(vararg suffixes: String): Boolean = suffixes.any(::endsWith)

public fun String.endsWithAny(suffixes: Iterable<String>): Boolean = suffixes.any(::endsWith)

public fun String.notStartsWithAny(vararg prefixes: String): Boolean = !this.startsWithAny(*prefixes)

public fun String.notStartsWithAny(prefixes: Iterable<String>): Boolean = !this.startsWithAny(prefixes)

public fun String.notEndsWithAny(vararg suffixes: String): Boolean = !this.endsWithAny(*suffixes)

public fun String.notEndsWithAny(suffixes: Iterable<String>): Boolean = !this.endsWithAny(suffixes)
