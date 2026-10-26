package pro.vlprojects.semantic.format.abbreviation

public fun interface Abbreviation {

    public fun shorten(source: String, visibleLength: Int): String
}
