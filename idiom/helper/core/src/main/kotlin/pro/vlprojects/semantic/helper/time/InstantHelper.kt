package pro.vlprojects.semantic.helper.time

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

public fun Instant.isCloseTo(other: Instant, tolerance: Duration): Boolean {
    require(!tolerance.isNegative) { "The tolerance must not be negative, got $tolerance" }
    return Duration
        .between(this, other)
        .abs() <= tolerance
}

public fun Instant.isOlderThan(age: Duration, clock: Clock = Clock.systemUTC()): Boolean = age
    .ago(clock)
    .let(::isBefore)

public fun Instant.isSameDayAs(other: Instant, zone: ZoneId): Boolean = this.toDateIn(zone) == other.toDateIn(zone)

public fun Instant.truncateTo(step: Duration): Instant = this
    .calculateRemainderOf(step)
    .let(::minus)

public fun Instant.ceilTo(step: Duration): Instant = this
    .calculateRemainderOf(step)
    .let { remainder ->
        when {
            remainder.isZero -> this
            else -> this.minus(remainder).plus(step)
        }
    }

public fun Instant.roundTo(step: Duration): Instant = this
    .calculateRemainderOf(step)
    .let { remainder ->
        when {
            remainder.multipliedBy(2) < step -> this.minus(remainder)
            else -> this.minus(remainder).plus(step)
        }
    }

private fun Instant.toDateIn(zone: ZoneId) = LocalDate.ofInstant(this, zone)

private fun Instant.calculateRemainderOf(step: Duration): Duration {
    require(step.isPositive) { "The step must be positive, got $step" }
    val elapsed = Duration.between(Instant.EPOCH, this)
    return elapsed
        .dividedBy(step)
        .let(step::multipliedBy)
        .let(elapsed::minus)
        .let { remainder ->
            when {
                remainder.isNegative -> remainder.plus(step)
                else -> remainder
            }
        }
}
