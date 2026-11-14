package pro.vlprojects.semantic.helper.unit.collection

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.collection.hasSameElementsAs
import pro.vlprojects.semantic.helper.collection.sumHashCodes

class CollectionHelperTests {

    @Test
    fun `the same elements in another order are the same`() {
        assertTrue(listOf(1, 2, 2).hasSameElementsAs(listOf(2, 1, 2)))
    }

    @Test
    fun `a repeated element is counted`() {
        assertFalse(listOf(1, 1, 2).hasSameElementsAs(listOf(1, 2, 2)))
    }

    @Test
    fun `a missing element makes them differ`() {
        assertFalse(listOf(1, 2).hasSameElementsAs(listOf(1, 3)))
        assertFalse(listOf(1, 2).hasSameElementsAs(listOf(1, 2, 3)))
    }

    @Test
    fun `the sum of hash codes does not depend on the order`() {
        assertEquals(listOf("a", "b", null).sumHashCodes(), listOf(null, "b", "a").sumHashCodes())
    }
}
