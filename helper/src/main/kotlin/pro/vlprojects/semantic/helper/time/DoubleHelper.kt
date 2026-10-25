package pro.vlprojects.semantic.helper.time

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

public fun Double.nanoseconds(): Duration = this.toDuration(DurationUnit.NANOSECONDS)

public fun Double.nanosecond(): Duration = this.nanoseconds()

public fun Double.microseconds(): Duration = this.toDuration(DurationUnit.MICROSECONDS)

public fun Double.microsecond(): Duration = this.microseconds()

public fun Double.milliseconds(): Duration = this.toDuration(DurationUnit.MILLISECONDS)

public fun Double.millisecond(): Duration = this.milliseconds()

public fun Double.seconds(): Duration = this.toDuration(DurationUnit.SECONDS)

public fun Double.second(): Duration = this.seconds()

public fun Double.minutes(): Duration = this.toDuration(DurationUnit.MINUTES)

public fun Double.minute(): Duration = this.minutes()

public fun Double.hours(): Duration = this.toDuration(DurationUnit.HOURS)

public fun Double.hour(): Duration = this.hours()

public fun Double.days(): Duration = this.toDuration(DurationUnit.DAYS)

public fun Double.day(): Duration = this.days()
