package pro.vlprojects.semantic.format.mask

public data object HideAll : MaskMode {

    override fun mask(source: String, mask: Char): String = HideMiddle
        .of()
        .mask(source, mask)
}
