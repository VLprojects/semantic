package pro.vlprojects.semantic.format.mask

public data object MaskEnding : MaskMode {

    override fun mask(positions: List<Int>, keepLength: Int): List<Int> = positions.drop(keepLength)
}
