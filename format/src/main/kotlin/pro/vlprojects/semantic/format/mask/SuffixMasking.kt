package pro.vlprojects.semantic.format.mask

public data object SuffixMasking : Masking {

    override fun cover(maskableIndices: List<Int>, visibleLength: Int): List<Int> = maskableIndices.drop(visibleLength)
}
