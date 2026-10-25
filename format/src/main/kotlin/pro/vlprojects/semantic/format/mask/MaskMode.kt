package pro.vlprojects.semantic.format.mask

public fun interface MaskMode {

    public fun mask(source: String): String = mask(source, '*')

    public fun mask(source: String, mask: Char): String
}
