package pro.vlprojects.semantic.format.mask

public data object HideAll : MaskMode {

    override fun mask(value: String, mask: Char): String = KeepSymbols().mask(value, mask)
}
