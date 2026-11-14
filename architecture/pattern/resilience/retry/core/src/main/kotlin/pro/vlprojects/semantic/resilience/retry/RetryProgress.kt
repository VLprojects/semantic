package pro.vlprojects.semantic.resilience.retry

import java.time.Duration

public data class RetryProgress(
    val name: String,
    val attempt: Int,
    val elapsed: Duration,
)
