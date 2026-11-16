package pro.vlprojects.semantic.resilience.circuitbreaker.unit.window

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class AdjustableClock(private var now: Instant = Instant.EPOCH) : Clock() {

    fun move(distance: Duration) {
        now += distance
    }

    override fun instant() = now

    override fun getZone() = ZoneOffset.UTC

    override fun withZone(zone: ZoneId) = this
}
