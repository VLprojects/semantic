package pro.vlprojects.semantic.resilience.circuitbreaker.window

public enum class CallOutcome(internal val statistics: CallStatistics) {

    SUCCESS(CallStatistics(total = 1, failed = 0, slow = 0)),
    SLOW_SUCCESS(CallStatistics(total = 1, failed = 0, slow = 1)),
    FAILURE(CallStatistics(total = 1, failed = 1, slow = 0)),
    SLOW_FAILURE(CallStatistics(total = 1, failed = 1, slow = 1)),
}
