package pro.vlprojects.semantic.format.mask

public data object SuffixMasking : Masking {

    override fun mask(maskableIndices: List<Int>, visibleLength: Int): List<Int> = maskableIndices.drop(visibleLength)
}
