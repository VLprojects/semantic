package pro.vlprojects.semantic.types.version

public class SequentialVersion private constructor(
    public val value: Long,
) : OrderedVersion<SequentialVersion>,
    Successive<SequentialVersion> {

    override fun next(): SequentialVersion = Math.addExact(value, 1L).let(::SequentialVersion)

    override fun compareTo(other: SequentialVersion): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is SequentialVersion -> false
        else -> value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    override fun toString(): String = value.toString()

    public companion object Format : VersionFormat<SequentialVersion> {

        private val CANONICAL = Regex("0|[1-9][0-9]*")

        @JvmField
        public val INITIAL: SequentialVersion = SequentialVersion(0)

        public fun of(value: Long): SequentialVersion = value
            .also { require(it >= 0) { "A sequential version is not negative: $it" } }
            .let(::SequentialVersion)

        override fun parseOrNull(text: String): SequentialVersion? = text
            .takeIf(CANONICAL::matches)
            ?.toLongOrNull()
            ?.let(::SequentialVersion)
    }
}
