package pro.vlprojects.semantic.resilience.backoff

internal fun requirePositiveAttempt(attempt: Int) {
    require(attempt >= 1) { "The attempt must be positive: $attempt" }
}
