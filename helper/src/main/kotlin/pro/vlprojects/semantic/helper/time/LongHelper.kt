package pro.vlprojects.semantic.helper.time

import java.time.Duration
import java.time.Period
import java.time.temporal.ChronoUnit

private const val DAYS_IN_WEEK = 7L

public fun Long.nanoseconds(): Duration = Duration.of(this, ChronoUnit.NANOS)

public fun Long.nanosecond(): Duration = this.nanoseconds()

public fun Long.microseconds(): Duration = Duration.of(this, ChronoUnit.MICROS)

public fun Long.microsecond(): Duration = this.microseconds()

public fun Long.milliseconds(): Duration = Duration.of(this, ChronoUnit.MILLIS)

public fun Long.millisecond(): Duration = this.milliseconds()

public fun Long.seconds(): Duration = Duration.of(this, ChronoUnit.SECONDS)

public fun Long.second(): Duration = this.seconds()

public fun Long.minutes(): Duration = Duration.of(this, ChronoUnit.MINUTES)

public fun Long.minute(): Duration = this.minutes()

public fun Long.hours(): Duration = Duration.of(this, ChronoUnit.HOURS)

public fun Long.hour(): Duration = this.hours()

public fun Long.days(): Duration = Duration.of(this, ChronoUnit.DAYS)

public fun Long.day(): Duration = this.days()

public fun Long.weeks(): Duration = this
    .days()
    .multipliedBy(DAYS_IN_WEEK)

public fun Long.week(): Duration = this.weeks()

public fun Long.months(): Period = this
    .let(Math::toIntExact)
    .months()

public fun Long.month(): Period = this.months()

public fun Long.quarters(): Period = this
    .let(Math::toIntExact)
    .quarters()

public fun Long.quarter(): Period = this.quarters()

public fun Long.years(): Period = this
    .let(Math::toIntExact)
    .years()

public fun Long.year(): Period = this.years()

public fun Long.decades(): Period = this
    .let(Math::toIntExact)
    .decades()

public fun Long.decade(): Period = this.decades()
