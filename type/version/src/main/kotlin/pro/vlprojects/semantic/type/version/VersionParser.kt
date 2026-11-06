package pro.vlprojects.semantic.type.version

public interface VersionParser<out V : Version> {

    public fun parseOrNull(text: String): V?

    public fun parse(text: String): V = parseOrNull(text)
        ?: throw IllegalArgumentException("Wrong version format: \"$text\"")
}
