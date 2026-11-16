package pro.vlprojects.semantic.helper.unit.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.time.ago
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class DurationHelperTests {

    @Test
    fun `a duration ago is that long before now`() {
        assertEquals(Instant.parse("2026-10-26T09:55:00Z"), Duration.ofMinutes(5).ago(CLOCK))
    }

    @Test
    fun `a negative duration ago is in the future`() {
        assertEquals(Instant.parse("2026-10-26T10:05:00Z"), Duration.ofMinutes(-5).ago(CLOCK))
    }

    private companion object {

        val NOW = Instant.parse("2026-10-26T10:00:00Z")

        val CLOCK = Clock.fixed(NOW, ZoneOffset.UTC)
    }
}
