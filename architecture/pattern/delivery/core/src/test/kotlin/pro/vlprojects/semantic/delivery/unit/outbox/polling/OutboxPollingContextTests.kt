package pro.vlprojects.semantic.delivery.unit.outbox.polling

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.polling.OutboxPollingContext
import java.time.Duration

class OutboxPollingContextTests {

    @Test
    fun `a batch of the batch size is full`() {
        assertTrue(OutboxPollingContext.of(100, 100, Duration.ZERO).isBatchFull())
    }

    @Test
    fun `a smaller batch is not full`() {
        assertFalse(OutboxPollingContext.of(99, 100, Duration.ZERO).isBatchFull())
    }

    @Test
    fun `the context keeps the previous delay`() {
        val context = OutboxPollingContext.of(0, 100, Duration.ofSeconds(1))

        assertEquals(Duration.ofSeconds(1), context.previousDelay)
    }

    @Test
    fun `the dispatched count must not exceed the batch size`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxPollingContext.of(101, 100, Duration.ZERO)
        }
    }

    @Test
    fun `the dispatched count must not be negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxPollingContext.of(-1, 100, Duration.ZERO)
        }
    }

    @Test
    fun `the batch size must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxPollingContext.of(0, 0, Duration.ZERO)
        }
    }

    @Test
    fun `the previous delay must not be negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxPollingContext.of(0, 100, Duration.ofMillis(-1))
        }
    }
}
