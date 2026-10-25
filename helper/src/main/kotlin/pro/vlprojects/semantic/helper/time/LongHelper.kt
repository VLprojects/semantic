package pro.vlprojects.semantic.helper.time

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

public fun Long.nanoseconds(): Duration = this.toDuration(DurationUnit.NANOSECONDS)

public fun Long.nanosecond(): Duration = this.nanoseconds()

public fun Long.microseconds(): Duration = this.toDuration(DurationUnit.MICROSECONDS)

public fun Long.microsecond(): Duration = this.microseconds()

public fun Long.milliseconds(): Duration = this.toDuration(DurationUnit.MILLISECONDS)

public fun Long.millisecond(): Duration = this.milliseconds()

public fun Long.seconds(): Duration = this.toDuration(DurationUnit.SECONDS)

public fun Long.second(): Duration = this.seconds()

public fun Long.minutes(): Duration = this.toDuration(DurationUnit.MINUTES)

public fun Long.minute(): Duration = this.minutes()

public fun Long.hours(): Duration = this.toDuration(DurationUnit.HOURS)

public fun Long.hour(): Duration = this.hours()

public fun Long.days(): Duration = this.toDuration(DurationUnit.DAYS)

public fun Long.day(): Duration = this.days()
