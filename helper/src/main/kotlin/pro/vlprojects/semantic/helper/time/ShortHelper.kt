package pro.vlprojects.semantic.helper.time

import kotlin.time.Duration

public fun Short.nanoseconds(): Duration = this
    .toInt()
    .nanoseconds()

public fun Short.nanosecond(): Duration = this.nanoseconds()

public fun Short.microseconds(): Duration = this
    .toInt()
    .microseconds()

public fun Short.microsecond(): Duration = this.microseconds()

public fun Short.milliseconds(): Duration = this
    .toInt()
    .milliseconds()

public fun Short.millisecond(): Duration = this.milliseconds()

public fun Short.seconds(): Duration = this
    .toInt()
    .seconds()

public fun Short.second(): Duration = this.seconds()

public fun Short.minutes(): Duration = this
    .toInt()
    .minutes()

public fun Short.minute(): Duration = this.minutes()

public fun Short.hours(): Duration = this
    .toInt()
    .hours()

public fun Short.hour(): Duration = this.hours()

public fun Short.days(): Duration = this
    .toInt()
    .days()

public fun Short.day(): Duration = this.days()
