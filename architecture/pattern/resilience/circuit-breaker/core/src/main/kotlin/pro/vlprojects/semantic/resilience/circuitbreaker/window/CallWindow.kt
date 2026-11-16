package pro.vlprojects.semantic.resilience.circuitbreaker.window

// Not thread-safe: the circuit breaker calls it under its own lock.
public interface CallWindow {

    public fun record(outcome: CallOutcome): CallStatistics

    public fun summarize(): CallStatistics

    public fun clear()
}
