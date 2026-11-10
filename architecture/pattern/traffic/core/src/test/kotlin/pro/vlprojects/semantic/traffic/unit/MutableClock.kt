package pro.vlprojects.semantic.traffic.unit

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

internal class MutableClock(private var instant: Instant) : Clock() {

    fun add(amount: Duration) {
        instant = instant.plus(amount)
    }

    override fun instant() = instant

    override fun getZone(): ZoneId = ZoneOffset.UTC

    override fun withZone(zone: ZoneId) = this
}
