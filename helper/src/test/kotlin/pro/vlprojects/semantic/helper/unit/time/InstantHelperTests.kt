package pro.vlprojects.semantic.helper.unit.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.helper.time.ceilTo
import pro.vlprojects.semantic.helper.time.isCloseTo
import pro.vlprojects.semantic.helper.time.isOlderThan
import pro.vlprojects.semantic.helper.time.isSameDayAs
import pro.vlprojects.semantic.helper.time.roundTo
import pro.vlprojects.semantic.helper.time.truncateTo
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class InstantHelperTests {

    @Test
    fun `moments within the tolerance are close in either order`() {
        assertTrue(NOW.isCloseTo(NOW.plusSeconds(2), Duration.ofSeconds(5)))
        assertTrue(NOW.plusSeconds(2).isCloseTo(NOW, Duration.ofSeconds(5)))
    }

    @Test
    fun `moments exactly the tolerance apart are close`() {
        assertTrue(NOW.isCloseTo(NOW.plusSeconds(5), Duration.ofSeconds(5)))
    }

    @Test
    fun `moments beyond the tolerance are not close`() {
        assertFalse(NOW.isCloseTo(NOW.plusSeconds(6), Duration.ofSeconds(5)))
    }

    @Test
    fun `a negative tolerance is refused`() {
        assertThrows<IllegalArgumentException> { NOW.isCloseTo(NOW, Duration.ofSeconds(-1)) }
    }

    @Test
    fun `a moment before the age is older than it`() {
        assertTrue(Instant.parse("2026-10-26T09:50:00Z").isOlderThan(Duration.ofMinutes(5), CLOCK))
    }

    @Test
    fun `a moment within the age or exactly at it is not older`() {
        assertFalse(Instant.parse("2026-10-26T09:58:00Z").isOlderThan(Duration.ofMinutes(5), CLOCK))
        assertFalse(Instant.parse("2026-10-26T09:55:00Z").isOlderThan(Duration.ofMinutes(5), CLOCK))
    }

    @Test
    fun `the same day depends on the zone`() {
        val evening = Instant.parse("2026-10-26T20:00:00Z")
        val night = Instant.parse("2026-10-26T22:00:00Z")
        assertTrue(evening.isSameDayAs(night, ZoneOffset.UTC))
        assertFalse(evening.isSameDayAs(night, MOSCOW))
    }

    @Test
    fun `a moment is truncated to the start of its step`() {
        assertEquals(Instant.parse("2026-10-26T10:30:00Z"), Instant.parse("2026-10-26T10:37:12Z").truncateTo(QUARTER))
    }

    @Test
    fun `a moment is ceiled to the end of its step`() {
        assertEquals(Instant.parse("2026-10-26T10:45:00Z"), Instant.parse("2026-10-26T10:37:12Z").ceilTo(QUARTER))
    }

    @Test
    fun `a moment is rounded to the nearest step and a half goes up`() {
        assertEquals(Instant.parse("2026-10-26T10:30:00Z"), Instant.parse("2026-10-26T10:37:29Z").roundTo(QUARTER))
        assertEquals(Instant.parse("2026-10-26T10:45:00Z"), Instant.parse("2026-10-26T10:37:30Z").roundTo(QUARTER))
    }

    @Test
    fun `a moment on a step stays as it is`() {
        val onStep = Instant.parse("2026-10-26T10:30:00Z")
        assertEquals(onStep, onStep.truncateTo(QUARTER))
        assertEquals(onStep, onStep.ceilTo(QUARTER))
        assertEquals(onStep, onStep.roundTo(QUARTER))
    }

    @Test
    fun `a moment before the epoch is truncated down`() {
        assertEquals(Instant.parse("1969-12-31T23:45:00Z"), Instant.parse("1969-12-31T23:59:50Z").truncateTo(QUARTER))
    }

    @Test
    fun `a step that is not positive is refused`() {
        assertThrows<IllegalArgumentException> { NOW.truncateTo(Duration.ZERO) }
        assertThrows<IllegalArgumentException> { NOW.roundTo(Duration.ofMinutes(-15)) }
    }

    private companion object {

        val NOW = Instant.parse("2026-10-26T10:00:00Z")

        val CLOCK = Clock.fixed(NOW, ZoneOffset.UTC)

        val QUARTER = Duration.ofMinutes(15)

        val MOSCOW = ZoneId.of("Europe/Moscow")
    }
}
