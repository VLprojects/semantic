package pro.vlprojects.semantic.format.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.format.pluralize
import pro.vlprojects.semantic.format.pluralizeWithCount

class IntFormatHelperTests {

    @Test
    fun `a count takes the form of one, few or many`() {
        assertEquals("день", 21.pluralize("день", "дня", "дней"))
        assertEquals("дня", 3.pluralize("день", "дня", "дней"))
        assertEquals("дней", 11.pluralize("день", "дня", "дней"))
    }

    @Test
    fun `a count is written before its form`() {
        assertEquals("21 день", 21.pluralizeWithCount("день", "дня", "дней"))
        assertEquals("12 дней", 12.pluralizeWithCount("день", "дня", "дней"))
    }
}
