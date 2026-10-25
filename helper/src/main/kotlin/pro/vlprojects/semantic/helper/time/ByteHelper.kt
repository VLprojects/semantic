package pro.vlprojects.semantic.helper.time

import java.time.Duration
import java.time.Period

public fun Byte.nanoseconds(): Duration = this
    .toLong()
    .nanoseconds()

public fun Byte.nanosecond(): Duration = this.nanoseconds()

public fun Byte.microseconds(): Duration = this
    .toLong()
    .microseconds()

public fun Byte.microsecond(): Duration = this.microseconds()

public fun Byte.milliseconds(): Duration = this
    .toLong()
    .milliseconds()

public fun Byte.millisecond(): Duration = this.milliseconds()

public fun Byte.seconds(): Duration = this
    .toLong()
    .seconds()

public fun Byte.second(): Duration = this.seconds()

public fun Byte.minutes(): Duration = this
    .toLong()
    .minutes()

public fun Byte.minute(): Duration = this.minutes()

public fun Byte.hours(): Duration = this
    .toLong()
    .hours()

public fun Byte.hour(): Duration = this.hours()

public fun Byte.days(): Duration = this
    .toLong()
    .days()

public fun Byte.day(): Duration = this.days()

public fun Byte.weeks(): Duration = this
    .toLong()
    .weeks()

public fun Byte.week(): Duration = this.weeks()

public fun Byte.months(): Period = this
    .toInt()
    .months()

public fun Byte.month(): Period = this.months()

public fun Byte.quarters(): Period = this
    .toInt()
    .quarters()

public fun Byte.quarter(): Period = this.quarters()

public fun Byte.years(): Period = this
    .toInt()
    .years()

public fun Byte.year(): Period = this.years()

public fun Byte.decades(): Period = this
    .toInt()
    .decades()

public fun Byte.decade(): Period = this.decades()
