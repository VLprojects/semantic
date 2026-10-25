package pro.vlprojects.semantic.helper.time

import java.time.Duration
import java.time.Period

public fun Short.nanoseconds(): Duration = this
    .toLong()
    .nanoseconds()

public fun Short.nanosecond(): Duration = this.nanoseconds()

public fun Short.microseconds(): Duration = this
    .toLong()
    .microseconds()

public fun Short.microsecond(): Duration = this.microseconds()

public fun Short.milliseconds(): Duration = this
    .toLong()
    .milliseconds()

public fun Short.millisecond(): Duration = this.milliseconds()

public fun Short.seconds(): Duration = this
    .toLong()
    .seconds()

public fun Short.second(): Duration = this.seconds()

public fun Short.minutes(): Duration = this
    .toLong()
    .minutes()

public fun Short.minute(): Duration = this.minutes()

public fun Short.hours(): Duration = this
    .toLong()
    .hours()

public fun Short.hour(): Duration = this.hours()

public fun Short.days(): Duration = this
    .toLong()
    .days()

public fun Short.day(): Duration = this.days()

public fun Short.weeks(): Duration = this
    .toLong()
    .weeks()

public fun Short.week(): Duration = this.weeks()

public fun Short.months(): Period = this
    .toInt()
    .months()

public fun Short.month(): Period = this.months()

public fun Short.quarters(): Period = this
    .toInt()
    .quarters()

public fun Short.quarter(): Period = this.quarters()

public fun Short.years(): Period = this
    .toInt()
    .years()

public fun Short.year(): Period = this.years()

public fun Short.decades(): Period = this
    .toInt()
    .decades()

public fun Short.decade(): Period = this.decades()
