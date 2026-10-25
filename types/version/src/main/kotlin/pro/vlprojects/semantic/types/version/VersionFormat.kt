package pro.vlprojects.semantic.types.version

public interface VersionFormat<out V : Version> {

    public fun parseOrNull(text: String): V?

    public fun parse(text: String): V = parseOrNull(text)
        ?: throw IllegalArgumentException("Not a version of this format: $text")
}
