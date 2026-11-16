package pro.vlprojects.semantic.resilience.bulkhead

import pro.vlprojects.semantic.resilience.policy.Policy
import java.lang.System.Logger.Level
import java.time.Duration
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

public class Bulkhead private constructor(
    public val limit: Int,
    public val waitLimit: Duration,
    public val name: String,
    private val listeners: List<BulkheadListener>,
) : Policy {

    init {
        require(limit >= 1) { "The limit must be positive: $limit" }
        require(!waitLimit.isNegative) { "The wait limit must not be negative: $waitLimit" }
    }

    private val semaphore = Semaphore(limit, IS_FAIR)
    private val waitNanos = waitLimit
        .coerceAtMost(LONGEST_WAIT)
        .toNanos()

    public val activeCalls: Int get() = limit - semaphore.availablePermits()

    public val waitingCalls: Int get() = semaphore.queueLength

    override fun <T> execute(action: () -> T): T {
        val startedAt = System.nanoTime()

        return when {
            semaphore.tryAcquire(waitNanos, TimeUnit.NANOSECONDS) -> runPermitted(startedAt, action)
            else -> reject(startedAt)
        }
    }

    override fun toString(): String = "Bulkhead '$name'"

    private fun measureSince(startedAt: Long) = System
        .nanoTime()
        .minus(startedAt)
        .let(Duration::ofNanos)

    private fun <T> runPermitted(startedAt: Long, action: () -> T): T {
        try {
            val waited = measureSince(startedAt)
            notifyListeners { listener -> listener.onPermitted(this, waited) }
            return action()
        } finally {
            semaphore.release()
            notifyListeners { listener -> listener.onFinished(this) }
        }
    }

    private fun reject(startedAt: Long): Nothing {
        val waited = measureSince(startedAt)
        notifyListeners { listener -> listener.onRejected(this, waited) }
        throw BulkheadFullException(name, limit, waitLimit)
    }

    private fun notifyListeners(event: (BulkheadListener) -> Unit) = listeners.forEach { listener ->
        try {
            event(listener)
        } catch (failure: InterruptedException) {
            Thread.currentThread().interrupt()
            logListenerFailure(failure)
        } catch (failure: Exception) {
            logListenerFailure(failure)
        }
    }

    private fun logListenerFailure(failure: Exception) = logger.log(
        Level.WARNING,
        "A listener of $this failed",
        failure,
    )

    public companion object Factory {

        private const val DEFAULT_NAME = "bulkhead"
        private const val IS_FAIR = true
        private val LONGEST_WAIT = Duration.ofNanos(Long.MAX_VALUE)
        private val logger = System.getLogger(Bulkhead::class.java.name)

        public fun of(
            limit: Int,
            waitLimit: Duration = Duration.ZERO,
            name: String = DEFAULT_NAME,
            listeners: List<BulkheadListener> = emptyList(),
        ): Bulkhead = Bulkhead(limit, waitLimit, name, listeners.toList())
    }
}
