package pro.vlprojects.semantic.format.mask

public fun String.masked(mode: MaskMode = KeepSymbols(last = 4), mask: Char = '*'): String = mode.mask(this, mask)
