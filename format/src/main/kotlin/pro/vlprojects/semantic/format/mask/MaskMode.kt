package pro.vlprojects.semantic.format.mask

public fun interface MaskMode {

    public fun mask(value: String, mask: Char): String
}
