package pro.vlprojects.semantic.format.mask

public data object HideBeginning : MaskMode {

    override fun mask(source: String, keepLength: Int, mask: Mask): String = source.hide(0, keepLength, mask)
}
