package pro.vlprojects.semantic.events

import kotlin.time.Duration

public fun interface RetryPolicy {

    public fun delayAfter(attempt: Int): Duration?
}
