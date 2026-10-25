package pro.vlprojects.semantic.helper.time

import kotlin.time.Duration

public fun Byte.nanoseconds(): Duration = this
    .toInt()
    .nanoseconds()

public fun Byte.nanosecond(): Duration = this.nanoseconds()

public fun Byte.microseconds(): Duration = this
    .toInt()
    .microseconds()

public fun Byte.microsecond(): Duration = this.microseconds()

public fun Byte.milliseconds(): Duration = this
    .toInt()
    .milliseconds()

public fun Byte.millisecond(): Duration = this.milliseconds()

public fun Byte.seconds(): Duration = this
    .toInt()
    .seconds()

public fun Byte.second(): Duration = this.seconds()

public fun Byte.minutes(): Duration = this
    .toInt()
    .minutes()

public fun Byte.minute(): Duration = this.minutes()

public fun Byte.hours(): Duration = this
    .toInt()
    .hours()

public fun Byte.hour(): Duration = this.hours()

public fun Byte.days(): Duration = this
    .toInt()
    .days()

public fun Byte.day(): Duration = this.days()
