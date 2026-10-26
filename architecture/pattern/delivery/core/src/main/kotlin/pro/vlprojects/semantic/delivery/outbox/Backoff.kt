package pro.vlprojects.semantic.delivery.outbox

import java.time.Duration

public fun interface Backoff {

    public fun delayAfter(attempt: Int): Duration
}
