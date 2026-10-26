package pro.vlprojects.semantic.format.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.format.pluralize
import pro.vlprojects.semantic.format.pluralizeWithCount

class LongFormatHelperTests {

    @Test
    fun `a count ending in one takes the form of one`() {
        listOf(1L, 21L, 101L, 1_000_001L).forEach {
            assertEquals("товар", it.pluralize("товар", "товара", "товаров"))
        }
    }

    @Test
    fun `a count ending in two to four takes the form of few`() {
        listOf(2L, 3L, 4L, 22L, 104L).forEach {
            assertEquals("товара", it.pluralize("товар", "товара", "товаров"))
        }
    }

    @Test
    fun `a count ending in zero or five to nine takes the form of many`() {
        listOf(0L, 5L, 9L, 20L, 100L).forEach {
            assertEquals("товаров", it.pluralize("товар", "товара", "товаров"))
        }
    }

    @Test
    fun `a count ending in eleven to fourteen takes the form of many`() {
        listOf(11L, 12L, 14L, 111L, 1_012L).forEach {
            assertEquals("товаров", it.pluralize("товар", "товара", "товаров"))
        }
    }

    @Test
    fun `a negative count takes the form of its absolute value`() {
        assertEquals("товар", (-1L).pluralize("товар", "товара", "товаров"))
        assertEquals("товара", (-22L).pluralize("товар", "товара", "товаров"))
        assertEquals("товаров", Long.MIN_VALUE.pluralize("товар", "товара", "товаров"))
    }

    @Test
    fun `a count is written before its form`() {
        assertEquals("21 день", 21L.pluralizeWithCount("день", "дня", "дней"))
        assertEquals("-3 дня", (-3L).pluralizeWithCount("день", "дня", "дней"))
    }
}
