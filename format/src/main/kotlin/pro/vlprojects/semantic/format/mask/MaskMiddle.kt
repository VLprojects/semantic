package pro.vlprojects.semantic.format.mask

public data object MaskMiddle : MaskMode {

    override fun mask(positions: List<Int>, keepLength: Int): List<Int> = positions
        .drop(keepLength - keepLength / 2)
        .dropLast(keepLength / 2)
}
