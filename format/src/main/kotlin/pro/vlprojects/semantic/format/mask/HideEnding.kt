package pro.vlprojects.semantic.format.mask

public data object HideEnding : MaskMode {

    override fun mask(source: String, keepLength: Int, mask: Mask): String = source.hide(keepLength, 0, mask)
}
