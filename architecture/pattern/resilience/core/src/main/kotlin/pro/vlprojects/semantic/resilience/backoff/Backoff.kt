package pro.vlprojects.semantic.resilience.backoff

import java.time.Duration

public fun interface Backoff {

    public fun delayAfter(attempt: Int): Duration
}
