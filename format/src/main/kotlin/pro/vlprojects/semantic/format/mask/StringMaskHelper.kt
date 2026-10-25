package pro.vlprojects.semantic.format.mask

public fun String.masked(mode: MaskMode = HideMiddle.of(keepLast = 4), mask: Char = '*'): String = mode.mask(this, mask)
