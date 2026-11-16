package pro.vlprojects.semantic.resilience.retry

import java.time.Duration

public sealed interface RetryDecision {

    public data class Wait(val delay: Duration) : RetryDecision

    public data class Stop(val reason: RetryStopReason) : RetryDecision
}
