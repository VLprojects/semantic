package pro.vlprojects.semantic.helper.time

import java.time.Duration
import java.time.Period

private const val MONTHS_IN_QUARTER = 3

private const val YEARS_IN_DECADE = 10

public fun Int.nanoseconds(): Duration = this
    .toLong()
    .nanoseconds()

public fun Int.nanosecond(): Duration = this.nanoseconds()

public fun Int.microseconds(): Duration = this
    .toLong()
    .microseconds()

public fun Int.microsecond(): Duration = this.microseconds()

public fun Int.milliseconds(): Duration = this
    .toLong()
    .milliseconds()

public fun Int.millisecond(): Duration = this.milliseconds()

public fun Int.seconds(): Duration = this
    .toLong()
    .seconds()

public fun Int.second(): Duration = this.seconds()

public fun Int.minutes(): Duration = this
    .toLong()
    .minutes()

public fun Int.minute(): Duration = this.minutes()

public fun Int.hours(): Duration = this
    .toLong()
    .hours()

public fun Int.hour(): Duration = this.hours()

public fun Int.days(): Duration = this
    .toLong()
    .days()

public fun Int.day(): Duration = this.days()

public fun Int.weeks(): Duration = this
    .toLong()
    .weeks()

public fun Int.week(): Duration = this.weeks()

public fun Int.months(): Period = Period.ofMonths(this)

public fun Int.month(): Period = this.months()

public fun Int.quarters(): Period = this
    .months()
    .multipliedBy(MONTHS_IN_QUARTER)

public fun Int.quarter(): Period = this.quarters()

public fun Int.years(): Period = Period.ofYears(this)

public fun Int.year(): Period = this.years()

public fun Int.decades(): Period = this
    .years()
    .multipliedBy(YEARS_IN_DECADE)

public fun Int.decade(): Period = this.decades()
