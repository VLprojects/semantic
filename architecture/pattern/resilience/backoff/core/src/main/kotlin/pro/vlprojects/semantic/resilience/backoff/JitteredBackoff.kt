package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration
import kotlin.random.Random

public class JitteredBackoff(
    private val backoff: Backoff,
    private val jitter: Double,
    private val randomizer: Random = Random.Default,
) : Backoff {

    init {
        require(jitter in 0.0..1.0) { "The jitter must be within 0..1: $jitter" }
    }

    override fun calculate(attempt: Int): Duration = attempt
        .let(backoff::calculate)
        .let(::randomize)

    private fun randomize(delay: Duration) = randomizer
        .nextDouble()
        .times(jitter)
        .let { reduction -> delay.toNanos() * (1 - reduction) }
        .let(Double::toLong)
        .let(Duration::ofNanos)

    public companion object Factory {

        public const val DEFAULT_JITTER: Double = 0.5

        public fun of(
            backoff: Backoff,
            jitter: Double = DEFAULT_JITTER,
            randomizer: Random = Random.Default,
        ): JitteredBackoff = JitteredBackoff(backoff, jitter, randomizer)
    }
}
