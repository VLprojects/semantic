package pro.vlprojects.semantic.ddd.tactical

/**
 * Identifier of a single value, usually a value class:
 *
 * ```kotlin
 * @JvmInline
 * value class OrderId(override val value: UUID) : Id<UUID>
 * ```
 */
public interface Id<out T : Any> : Identifier {
    public val value: T
}
