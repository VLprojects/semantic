package pro.vlprojects.semantic.delivery.unit.outbox.annotation

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.annotation.IdempotencyKey

class IdempotencyKeyTests {

    private class Payment(@IdempotencyKey val operation: String)

    private interface Refund {

        @get:IdempotencyKey
        val operation: String
    }

    @Test
    fun `java reflection sees the key on a field of a class`() {
        val field = Payment::class.java.getDeclaredField("operation")

        assertTrue(field.isAnnotationPresent(IdempotencyKey::class.java))
    }

    @Test
    fun `java reflection sees the key on a getter of an interface`() {
        val getter = Refund::class.java.getMethod("getOperation")

        assertTrue(getter.isAnnotationPresent(IdempotencyKey::class.java))
    }
}
