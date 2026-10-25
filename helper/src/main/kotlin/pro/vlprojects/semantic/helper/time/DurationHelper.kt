package pro.vlprojects.semantic.helper.time

import java.time.Clock
import java.time.Duration
import java.time.Instant

public fun Duration.ago(clock: Clock = Clock.systemUTC()): Instant = clock
    .instant()
    .minus(this)
