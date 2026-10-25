package pro.vlprojects.semantic.format.mask

public fun interface Masking {

    public fun mask(maskableIndices: List<Int>, visibleLength: Int): List<Int>
}
