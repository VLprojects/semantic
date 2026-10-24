package pro.vlprojects.semantic.ddd.unit.tactical

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.architecture.error.BusinessRuleException
import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.tactical.Specification

class SpecificationTests {

    private data object Positive : Specification<Int> {

        override fun isSatisfiedBy(candidate: Int): Boolean = candidate > 0
    }

    private data object NonEmpty : Specification<Collection<*>> {

        override fun isSatisfiedBy(candidate: Collection<*>): Boolean = candidate.isNotEmpty()
    }

    @Test
    fun `a specification checks a candidate`() {
        assertTrue(Positive.isSatisfiedBy(1))
        assertFalse(Positive.isSatisfiedBy(-1))
    }

    @Test
    fun `a specification is a predicate`() {
        assertTrue(Positive(1))
    }

    @Test
    fun `a specification filters a collection`() {
        val numbers = listOf(-1, 1, 2)

        assertEquals(listOf(1, 2), numbers.filter(Positive))
        assertEquals(1, numbers.count { !Positive(it) })
    }

    @Test
    fun `a specification of a supertype checks a subtype`() {
        val nonEmpty: Specification<List<Int>> = NonEmpty

        assertTrue(nonEmpty.isSatisfiedBy(listOf(1)))
        assertFalse(nonEmpty.isSatisfiedBy(emptyList()))
    }

    @Test
    fun `a specification makes a business rule`() {
        val failure = assertThrows<BusinessRuleException> { rule(Positive) { "$this is not positive" }.check(-1) }

        assertEquals("-1 is not positive", failure.message)
    }
}
