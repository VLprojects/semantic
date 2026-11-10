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
        assertTrue(OutboxPollingContext.of(dispatched = 100, batchSize = 100).isBatchFull())
    }

    @Test
    fun `a smaller batch is not full`() {
        assertFalse(OutboxPollingContext.of(dispatched = 99, batchSize = 100).isBatchFull())
    }

    @Test
    fun `the first batch has no previous delay`() {
        assertEquals(Duration.ZERO, OutboxPollingContext.of(dispatched = 0, batchSize = 100).previousDelay)
    }

    @Test
    fun `the dispatched count must not exceed the batch size`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxPollingContext.of(dispatched = 101, batchSize = 100)
        }
    }

    @Test
    fun `the dispatched count must not be negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxPollingContext.of(dispatched = -1, batchSize = 100)
        }
    }

    @Test
    fun `the batch size must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxPollingContext.of(dispatched = 0, batchSize = 0)
        }
    }

    @Test
    fun `the previous delay must not be negative`() {
        assertThrows(IllegalArgumentException::class.java) {
            OutboxPollingContext.of(dispatched = 0, batchSize = 100, previousDelay = Duration.ofMillis(-1))
        }
    }
}
