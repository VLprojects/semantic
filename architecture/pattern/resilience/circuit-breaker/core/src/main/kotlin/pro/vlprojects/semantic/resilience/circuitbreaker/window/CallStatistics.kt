package pro.vlprojects.semantic.resilience.circuitbreaker.window

import java.util.Objects

public class CallStatistics(
    public val total: Long,
    public val failed: Long,
    public val slow: Long,
) {

    init {
        require(total >= 0) { "The total must not be negative: $total" }
        require(failed in 0..total) { "The failed calls must be within 0..$total: $failed" }
        require(slow in 0..total) { "The slow calls must be within 0..$total: $slow" }
    }

    public val failureRate: Double
        get() = calculateRate(failed)

    public val slowRate: Double
        get() = calculateRate(slow)

    internal operator fun plus(other: CallStatistics) = CallStatistics(
        total = total + other.total,
        failed = failed + other.failed,
        slow = slow + other.slow,
    )

    internal operator fun minus(other: CallStatistics) = CallStatistics(
        total = total - other.total,
        failed = failed - other.failed,
        slow = slow - other.slow,
    )

    private fun calculateRate(count: Long) = when (total) {
        0L -> 0.0
        else -> count.toDouble() / total
    }

    override fun equals(other: Any?): Boolean = other is CallStatistics &&
        total == other.total &&
        failed == other.failed &&
        slow == other.slow

    override fun hashCode(): Int = Objects.hash(total, failed, slow)

    override fun toString(): String = "CallStatistics(total=$total, failed=$failed, slow=$slow)"

    internal companion object {

        val EMPTY = CallStatistics(total = 0, failed = 0, slow = 0)
    }
}
