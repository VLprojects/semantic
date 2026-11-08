package pro.vlprojects.semantic.resilience.backoff.extension

import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.JitteredBackoff
import kotlin.random.Random

public fun Backoff.jittered(
    jitter: Double = JitteredBackoff.DEFAULT_JITTER,
    randomizer: Random = Random.Default,
): Backoff = JitteredBackoff.of(this, jitter, randomizer)
