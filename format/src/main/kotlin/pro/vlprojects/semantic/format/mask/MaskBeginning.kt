package pro.vlprojects.semantic.format.mask

public data object MaskBeginning : MaskMode {

    override fun mask(source: String, keepLength: Int, mask: Mask): String = source.hide(0, keepLength, mask)
}
