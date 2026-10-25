package pro.vlprojects.semantic.ddd.unit.tactical

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.ddd.architecture.error.BusinessRuleException
import pro.vlprojects.semantic.ddd.dsl.rule
import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.specification.composite.AllOf
import pro.vlprojects.semantic.ddd.tactical.specification.composite.Always
import pro.vlprojects.semantic.ddd.tactical.specification.composite.Never

class SpecificationTests {

    private data object Positive : Specification<Int> {

        override fun isSatisfiedBy(candidate: Int): Boolean = candidate > 0
    }

    private data object Even : Specification<Int> {

        override fun isSatisfiedBy(candidate: Int): Boolean = candidate % 2 == 0
    }

    private data object Small : Specification<Int> {

        override fun isSatisfiedBy(candidate: Int): Boolean = candidate < 10
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

    @Test
    fun `and holds when both hold`() {
        assertTrue((Positive and Even).isSatisfiedBy(2))
        assertFalse((Positive and Even).isSatisfiedBy(1))
    }

    @Test
    fun `or holds when one holds`() {
        assertTrue((Positive or Even).isSatisfiedBy(-2))
        assertFalse((Positive or Even).isSatisfiedBy(-1))
    }

    @Test
    fun `not inverts`() {
        assertTrue((!Positive).isSatisfiedBy(-1))
        assertFalse((!Positive).isSatisfiedBy(1))
    }

    @Test
    fun `nested conjunctions are flattened`() {
        val flat = (Positive and Even) and Small

        assertEquals(AllOf(listOf(Positive, Even, Small)), flat)
    }

    @Test
    fun `neutral elements are dropped and absorbing ones win`() {
        val positive: Specification<Int> = Positive

        assertEquals(Positive, positive and Always)
        assertEquals(Never, positive and Never)
        assertEquals(Positive, positive or Never)
        assertEquals(Always, positive or Always)
    }

    @Test
    fun `double negation cancels out`() {
        assertEquals(Positive, !!Positive)
        assertEquals(Never, !Always)
        assertEquals(Always, !Never)
    }

    @Test
    fun `combinations are equal regardless of the order`() {
        assertEquals(Positive and Even, Even and Positive)
        assertEquals(Positive or Even, Even or Positive)
    }

    @Test
    fun `infix operators bind left to right`() {
        assertEquals((Positive or Even) and Small, Positive or Even and Small)
    }

    @Test
    fun `a long chain of and or or keeps the stack shallow`() {
        val positive: Specification<Int> = Positive
        val conjunction = (1..100_000).fold(positive) { chain, _ -> chain and Even }
        val disjunction = (1..100_000).fold(positive) { chain, _ -> chain or Even }

        assertTrue(conjunction.isSatisfiedBy(2))
        assertTrue(disjunction.isSatisfiedBy(-2))
        assertEquals(100_001, (conjunction as AllOf).specifications.size)
    }
}
