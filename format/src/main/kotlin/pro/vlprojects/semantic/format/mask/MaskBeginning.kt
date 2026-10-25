package pro.vlprojects.semantic.format.mask

public data object MaskBeginning : MaskMode {

    override fun mask(positions: List<Int>, keepLength: Int): List<Int> = positions.dropLast(keepLength)
}
