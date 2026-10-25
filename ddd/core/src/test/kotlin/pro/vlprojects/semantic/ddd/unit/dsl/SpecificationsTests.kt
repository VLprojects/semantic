package pro.vlprojects.semantic.ddd.unit.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.dsl.allOf
import pro.vlprojects.semantic.ddd.dsl.anyOf
import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.specification.composite.Always
import pro.vlprojects.semantic.ddd.tactical.specification.composite.Never

class SpecificationsTests {

    private data object Positive : Specification<Int> {

        override fun isSatisfiedBy(candidate: Int): Boolean = candidate > 0
    }

    private data object Even : Specification<Int> {

        override fun isSatisfiedBy(candidate: Int): Boolean = candidate % 2 == 0
    }

    private data object Small : Specification<Int> {

        override fun isSatisfiedBy(candidate: Int): Boolean = candidate < 10
    }

    @Test
    fun `all of is the and of its parts`() {
        assertEquals(Positive and Even and Small, allOf(Positive, Even, Small))
    }

    @Test
    fun `any of is the or of its parts`() {
        assertEquals(Positive or Even or Small, anyOf(Positive, Even, Small))
    }

    @Test
    fun `a list of specifications combines the same way`() {
        val parts = listOf(Positive, Even)

        assertEquals(allOf(Positive, Even), parts.allOf())
        assertEquals(anyOf(Positive, Even), parts.anyOf())
    }

    @Test
    fun `nothing to combine gives the neutral element`() {
        assertEquals(Always, allOf<Int>())
        assertEquals(Never, anyOf<Int>())
    }

    @Test
    fun `a single part stands for itself`() {
        assertEquals(Positive, allOf(Positive))
        assertEquals(Positive, anyOf(Positive))
    }

    @Test
    fun `optional filters drop out`() {
        val minimum: Int? = null
        val filters = listOfNotNull(Positive, minimum?.let { Small })

        assertEquals(Positive, filters.allOf())
    }
}
