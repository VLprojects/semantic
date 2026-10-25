package pro.vlprojects.semantic.helper.time

import kotlin.time.Duration

public fun Float.nanoseconds(): Duration = this
    .toDouble()
    .nanoseconds()

public fun Float.nanosecond(): Duration = this.nanoseconds()

public fun Float.microseconds(): Duration = this
    .toDouble()
    .microseconds()

public fun Float.microsecond(): Duration = this.microseconds()

public fun Float.milliseconds(): Duration = this
    .toDouble()
    .milliseconds()

public fun Float.millisecond(): Duration = this.milliseconds()

public fun Float.seconds(): Duration = this
    .toDouble()
    .seconds()

public fun Float.second(): Duration = this.seconds()

public fun Float.minutes(): Duration = this
    .toDouble()
    .minutes()

public fun Float.minute(): Duration = this.minutes()

public fun Float.hours(): Duration = this
    .toDouble()
    .hours()

public fun Float.hour(): Duration = this.hours()

public fun Float.days(): Duration = this
    .toDouble()
    .days()

public fun Float.day(): Duration = this.days()
