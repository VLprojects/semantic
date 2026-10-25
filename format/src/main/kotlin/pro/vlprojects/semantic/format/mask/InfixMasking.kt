package pro.vlprojects.semantic.format.mask

public data object InfixMasking : Masking {

    override fun mask(maskableIndices: List<Int>, visibleLength: Int): List<Int> = maskableIndices
        .drop(visibleLength - visibleLength / 2)
        .dropLast(visibleLength / 2)
}
