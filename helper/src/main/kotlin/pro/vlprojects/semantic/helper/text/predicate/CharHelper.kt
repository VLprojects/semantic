package pro.vlprojects.semantic.helper.text.predicate

private const val LAST_ASCII_CODE = 127

public fun Char.isAscii(): Boolean = this.code <= LAST_ASCII_CODE

public fun Char.isSpace(): Boolean = this == ' '

public fun Char.isSpaceSeparator(): Boolean = this.category == CharCategory.SPACE_SEPARATOR

public fun Char.isHorizontalTab(): Boolean = this == '\t'

public fun Char.isNewline(): Boolean = this == '\n'

public fun Char.isCarriageReturn(): Boolean = this == '\r'

public fun Char.isApostrophe(): Boolean = this == '\''

public fun Char.isQuote(): Boolean = this == '"'

public fun Char.isEquals(): Boolean = this == '='

public fun Char.isComma(): Boolean = this == ','

public fun Char.isSemicolon(): Boolean = this == ';'

public fun Char.isDot(): Boolean = this == '.'

public fun Char.isHyphen(): Boolean = this == '-'

public fun Char.isUnderscore(): Boolean = this == '_'

public fun Char.isWildcard(): Boolean = this == '*'

public fun Char.isOpeningParenthesis(): Boolean = this == '('

public fun Char.isClosingParenthesis(): Boolean = this == ')'

public fun Char.isParenthesis(): Boolean = when {
    this.isOpeningParenthesis() -> true
    this.isClosingParenthesis() -> true
    else -> false
}

public fun Char.isOpeningBracket(): Boolean = this == '['

public fun Char.isClosingBracket(): Boolean = this == ']'

public fun Char.isBracket(): Boolean = when {
    this.isOpeningBracket() -> true
    this.isClosingBracket() -> true
    else -> false
}
