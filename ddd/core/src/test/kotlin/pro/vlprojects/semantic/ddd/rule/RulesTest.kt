package pro.vlprojects.semantic.ddd.rule

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.error.BusinessRuleException

class RulesTest {
    private val positive = rule<Int>({ this > 0 }) { "$this is not positive" }
    private val even = rule<Int>({ this % 2 == 0 }) { "$this is not even" }

    @Test
    fun `a rule that holds passes`() {
        assertDoesNotThrow { positive.check(2) }
    }

    @Test
    fun `a broken rule throws its message`() {
        val failure = assertThrows<BusinessRuleException> { positive.check(-1) }

        assertEquals("-1 is not positive", failure.message)
    }

    @Test
    fun `a list of rules throws on the first one broken`() {
        val failure = assertThrows<BusinessRuleException> { listOf(positive, even).check(-1) }

        assertEquals("-1 is not positive", failure.message)
    }

    @Test
    fun `checkRule throws unless the condition holds`() {
        val failure = assertThrows<BusinessRuleException> { checkRule(false) { "Broken" } }

        assertEquals("Broken", failure.message)
    }

    @Test
    fun `checkRule smart casts what it checked`() {
        val paidAt = listOf("2026-10-23").firstOrNull()

        checkRule(paidAt != null) { "Not paid" }

        assertEquals(10, paidAt.length)
    }
}
