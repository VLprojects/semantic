package pro.vlprojects.semantic.resilience.retry

public enum class RetryStopReason {
    NOT_RETRYABLE,
    ATTEMPTS_EXHAUSTED,
    TIME_EXHAUSTED,
    INTERRUPTED,
}
