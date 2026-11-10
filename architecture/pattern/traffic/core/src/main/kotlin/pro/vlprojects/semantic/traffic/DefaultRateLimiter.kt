package pro.vlprojects.semantic.traffic

import org.slf4j.LoggerFactory
import pro.vlprojects.semantic.traffic.error.RateLimitOptimisticLockException
import java.time.Clock
import java.time.Duration

public class DefaultRateLimiter(
    override val name: String,
    private val algorithm: RateLimitAlgorithm,
    private val repository: RateLimitRepository,
    private val clock: Clock = Clock.systemUTC(),
    private val maxAttempts: Int = 8,
    private val onFailure: RateLimitFailureMode = RateLimitFailureMode.ALLOW,
) : RateLimiter {

    init {
        require(maxAttempts >= 1) { "The maximum of attempts must be positive: $maxAttempts" }
    }

    private val logger = LoggerFactory.getLogger(DefaultRateLimiter::class.java)

    override fun tryAcquire(key: String, permits: Int): RateLimitResponse {
        require(permits in 1..algorithm.permitLimit) { "Permits must be from 1 to ${algorithm.permitLimit}: $permits" }

        return try {
            acquireByVersion("$name:$key", permits)
        } catch (failure: Exception) {
            applyFailureMode(failure)
        }
    }

    // The last attempt lets the conflict through, so it is handled as a failure of the storage.
    private fun acquireByVersion(key: String, permits: Int) = (1 until maxAttempts)
        .firstNotNullOfOrNull { _ -> acquireOrNullOnConflict(key, permits) }
        ?: acquire(key, permits)

    private fun acquireOrNullOnConflict(key: String, permits: Int) = try {
        acquire(key, permits)
    } catch (_: RateLimitOptimisticLockException) {
        null
    }

    private fun acquire(key: String, permits: Int): RateLimitResponse {
        val now = clock.instant()
        val counter = repository.findByKey(key)
        val result = algorithm.tryAcquire(counter?.state, permits, now)
        val response = result.response

        if (response.isAcquired) {
            val expiresAt = now.plus(response.resetAfter)

            RateLimitCounter
                .of(key, result.state, counter?.version ?: 0L, expiresAt)
                .let(repository::save)
        }

        return response
    }

    private fun applyFailureMode(failure: Exception): RateLimitResponse {
        logger
            .atWarn()
            .setCause(failure)
            .log { "Rate limiter $name failed to update its counter and applies $onFailure" }

        return when (onFailure) {
            RateLimitFailureMode.ALLOW -> RateLimitResponse.acquired(remaining = 0, resetAfter = Duration.ZERO)

            RateLimitFailureMode.REJECT -> RateLimitResponse.rejected(
                remaining = 0,
                retryAfter = Duration.ZERO,
                resetAfter = Duration.ZERO,
            )
        }
    }
}
