package pro.vlprojects.semantic.resilience.circuitbreaker.window

public class CountWindow(size: Int) : CallWindow {

    init {
        require(size > 0) { "The size must be positive: $size" }
    }

    private val calls = Array(size) { CallStatistics.EMPTY }
    private var oldest = 0
    private var statistics = CallStatistics.EMPTY

    // O(1) time and O(size) memory: the oldest call is subtracted from the running total.
    override fun record(outcome: CallOutcome): CallStatistics {
        statistics = statistics - calls[oldest] + outcome.statistics
        calls[oldest] = outcome.statistics
        oldest = (oldest + 1) % calls.size
        return statistics
    }

    override fun summarize(): CallStatistics = statistics

    override fun clear() {
        calls.fill(CallStatistics.EMPTY)
        oldest = 0
        statistics = CallStatistics.EMPTY
    }

    public companion object Factory {

        private const val DEFAULT_SIZE = 100

        public fun of(size: Int = DEFAULT_SIZE): CountWindow = CountWindow(size)
    }
}
