package pro.vlprojects.semantic.format.mask

public fun interface MaskMode {

    public fun mask(source: String, keepLength: Int): String = mask(source, keepLength, Mask.of())

    public fun mask(source: String, keepLength: Int, mask: Mask): String
}
