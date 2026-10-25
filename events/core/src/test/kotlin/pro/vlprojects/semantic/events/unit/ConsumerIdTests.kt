package pro.vlprojects.semantic.events.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.events.ConsumerId

class ConsumerIdTests {

    @Test
    fun `ids with the same value are equal`() {
        assertEquals(ConsumerId("billing"), ConsumerId("billing"))
        assertNotEquals(ConsumerId("billing"), ConsumerId("shipping"))
    }

    @Test
    fun `an id is written as its value`() {
        assertEquals("billing", ConsumerId("billing").toString())
    }

    @Test
    fun `a blank id is refused`() {
        assertThrows<IllegalArgumentException> { ConsumerId(" ") }
    }
}
