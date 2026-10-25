package pro.vlprojects.semantic.helper.time

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

public fun Int.nanoseconds(): Duration = this.toDuration(DurationUnit.NANOSECONDS)

public fun Int.nanosecond(): Duration = this.nanoseconds()

public fun Int.microseconds(): Duration = this.toDuration(DurationUnit.MICROSECONDS)

public fun Int.microsecond(): Duration = this.microseconds()

public fun Int.milliseconds(): Duration = this.toDuration(DurationUnit.MILLISECONDS)

public fun Int.millisecond(): Duration = this.milliseconds()

public fun Int.seconds(): Duration = this.toDuration(DurationUnit.SECONDS)

public fun Int.second(): Duration = this.seconds()

public fun Int.minutes(): Duration = this.toDuration(DurationUnit.MINUTES)

public fun Int.minute(): Duration = this.minutes()

public fun Int.hours(): Duration = this.toDuration(DurationUnit.HOURS)

public fun Int.hour(): Duration = this.hours()

public fun Int.days(): Duration = this.toDuration(DurationUnit.DAYS)

public fun Int.day(): Duration = this.days()
