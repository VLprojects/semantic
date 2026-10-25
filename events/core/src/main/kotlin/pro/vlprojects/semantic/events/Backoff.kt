package pro.vlprojects.semantic.events

import kotlin.time.Duration

public fun interface Backoff {

    public fun delayAfter(attempt: Int): Duration
}
