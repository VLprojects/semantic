package pro.vlprojects.semantic.format.mask

public data object HideMiddle : MaskMode {

    override fun mask(source: String, keepLength: Int, mask: Mask): String =
        source.hide(keepLength - keepLength / 2, keepLength / 2, mask)
}
