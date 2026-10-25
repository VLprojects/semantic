package pro.vlprojects.semantic.ddd.spring.data.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AbstractSpringDataEntityTests {

    @Test
    fun `the id is the identifier`() {
        assertEquals(ItemId(1), Item(ItemId(1)).id)
    }
}
