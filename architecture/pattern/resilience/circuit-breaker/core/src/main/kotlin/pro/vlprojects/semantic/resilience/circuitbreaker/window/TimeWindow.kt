package pro.vlprojects.semantic.resilience.circuitbreaker.window

import java.time.Clock
import java.time.Duration

public class TimeWindow(
    span: Duration,
    bucket: Duration,
    private val clock: Clock,
) : CallWindow {

    init {
        require(bucket >= MINIMAL_BUCKET) { "The bucket must be at least $MINIMAL_BUCKET: $bucket" }
        require(bucket.toMillis().let(Duration::ofMillis) == bucket) {
            "The bucket must be whole milliseconds: $bucket"
        }
        require(span.isPositive) { "The span must be positive: $span" }
        require(span.dividedBy(bucket).let(bucket::multipliedBy) == span) {
            "The span $span is not a multiple of the bucket $bucket"
        }
        require(span.dividedBy(bucket) in MINIMAL_BUCKETS..MAXIMAL_BUCKETS) {
            "The span $span must hold $MINIMAL_BUCKETS..$MAXIMAL_BUCKETS buckets of $bucket"
        }
    }

    private val bucketMillis = bucket.toMillis()
    private val buckets = span
        .dividedBy(bucket)
        .toInt()
        .let { size ->
            Array(size) { CallStatistics.EMPTY }
        }
    private var currentIndex = 0
    private var currentSlot = readSlot()
    private var statistics = CallStatistics.EMPTY

    // O(min(elapsed, buckets)) time and O(buckets) memory: an expired bucket is subtracted from the running total.
    override fun record(outcome: CallOutcome): CallStatistics {
        advance()
        buckets[currentIndex] += outcome.statistics
        statistics += outcome.statistics
        return statistics
    }

    override fun summarize(): CallStatistics {
        advance()
        return statistics
    }

    override fun clear() {
        buckets.fill(CallStatistics.EMPTY)
        currentIndex = 0
        currentSlot = readSlot()
        statistics = CallStatistics.EMPTY
    }

    // The steps are capped by the buckets, so a long silence costs no more than one full turn.
    private fun advance() {
        val slot = readSlot()
        countExpiredBuckets(slot).let(::expireBuckets)
        currentSlot = slot
    }

    // A clock moved back yields a negative distance, and the calls go to the current bucket.
    private fun countExpiredBuckets(slot: Long) = slot
        .minus(currentSlot)
        .coerceIn(0, buckets.size.toLong())
        .toInt()

    private fun expireBuckets(count: Int) = repeat(count) {
        expireNextBucket()
    }

    private fun expireNextBucket() {
        currentIndex = (currentIndex + 1) % buckets.size
        statistics -= buckets[currentIndex]
        buckets[currentIndex] = CallStatistics.EMPTY
    }

    private fun readSlot() = clock
        .millis()
        .floorDiv(bucketMillis)

    public companion object Factory {

        private const val MINIMAL_BUCKETS = 2L
        private const val MAXIMAL_BUCKETS = 100_000L
        private val MINIMAL_BUCKET = Duration.ofMillis(1)
        private val DEFAULT_SPAN = Duration.ofMinutes(1)
        private val DEFAULT_BUCKET = Duration.ofSeconds(1)

        public fun of(
            span: Duration = DEFAULT_SPAN,
            bucket: Duration = DEFAULT_BUCKET,
            clock: Clock = Clock.systemUTC(),
        ): TimeWindow = TimeWindow(span, bucket, clock)
    }
}
