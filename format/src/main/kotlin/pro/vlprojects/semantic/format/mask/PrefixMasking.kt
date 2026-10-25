package pro.vlprojects.semantic.format.mask

public data object PrefixMasking : Masking {

    override fun mask(maskableIndices: List<Int>, visibleLength: Int): List<Int> =
        maskableIndices.dropLast(visibleLength)
}
