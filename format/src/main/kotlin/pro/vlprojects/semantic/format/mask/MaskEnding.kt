package pro.vlprojects.semantic.format.mask

public data object MaskEnding : MaskMode {

    override fun mask(source: String, keepLength: Int, mask: Mask): String = source.mask(keepLength, 0, mask)
}
