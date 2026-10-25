package pro.vlprojects.semantic.helper.unit.text.predicate

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.helper.text.predicate.endsWithAny
import pro.vlprojects.semantic.helper.text.predicate.isAscii
import pro.vlprojects.semantic.helper.text.predicate.isLowercase
import pro.vlprojects.semantic.helper.text.predicate.isNotAscii
import pro.vlprojects.semantic.helper.text.predicate.notEndsWithAny
import pro.vlprojects.semantic.helper.text.predicate.notStartsWithAny
import pro.vlprojects.semantic.helper.text.predicate.startsWithAny

class StringHelperTests {

    @Test
    fun `a value of latin letters, digits and punctuation is ascii`() {
        assertTrue("order-42_id.".isAscii())
        assertFalse("order-42_id.".isNotAscii())
    }

    @Test
    fun `a value with an accent or an emoji is not ascii`() {
        assertTrue("Café".isNotAscii())
        assertTrue("a😀".isNotAscii())
    }

    @Test
    fun `an empty value is ascii`() {
        assertTrue("".isAscii())
    }

    @Test
    fun `a value starts with any of the prefixes`() {
        assertTrue("order-42".startsWithAny("user-", "order-"))
        assertFalse("order-42".notStartsWithAny("user-", "order-"))
    }

    @Test
    fun `a value starts with none of the prefixes`() {
        assertFalse("order-42".startsWithAny("user-", "cart-"))
        assertTrue("order-42".notStartsWithAny("user-", "cart-"))
    }

    @Test
    fun `a value ends with any of the suffixes`() {
        assertTrue("report.pdf".endsWithAny(".doc", ".pdf"))
        assertFalse("report.pdf".notEndsWithAny(".doc", ".pdf"))
    }

    @Test
    fun `a value ends with none of the suffixes`() {
        assertFalse("report.pdf".endsWithAny(".doc", ".txt"))
        assertTrue("report.pdf".notEndsWithAny(".doc", ".txt"))
    }

    @Test
    fun `no prefixes and no suffixes match nothing`() {
        assertFalse("order-42".startsWithAny())
        assertFalse("order-42".endsWithAny())
    }

    @Test
    fun `the prefixes may come as a collection`() {
        assertTrue("order-42".startsWithAny(listOf("user-", "order-")))
        assertTrue("order-42".notStartsWithAny(setOf("user-", "cart-")))
    }

    @Test
    fun `the suffixes may come as a collection`() {
        assertTrue("report.pdf".endsWithAny(listOf(".doc", ".pdf")))
        assertTrue("report.pdf".notEndsWithAny(setOf(".doc", ".txt")))
    }

    @Test
    fun `an empty collection matches nothing`() {
        assertFalse("order-42".startsWithAny(emptyList()))
        assertFalse("order-42".endsWithAny(emptySet()))
    }

    @Test
    fun `a value without capitals is lowercase`() {
        assertTrue("order-id 42".isLowercase())
        assertTrue("заказ".isLowercase())
        assertTrue("".isLowercase())
    }

    @Test
    fun `a value with a capital is not lowercase`() {
        assertFalse("orderId".isLowercase())
        assertFalse("Заказ".isLowercase())
        assertFalse("\u01C5".isLowercase())
    }
}
