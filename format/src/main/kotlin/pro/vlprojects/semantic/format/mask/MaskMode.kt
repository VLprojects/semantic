package pro.vlprojects.semantic.format.mask

public fun interface MaskMode {

    public fun mask(positions: List<Int>, keepLength: Int): List<Int>
}
