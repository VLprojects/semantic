package pro.vlprojects.semantic.ddd.unit.tactical.specification.composite

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.specification.composite.Always
import pro.vlprojects.semantic.ddd.tactical.specification.composite.Never

class NeutralTests {

    @Test
    fun `always holds for any candidate`() {
        val always: Specification<Int> = Always

        assertTrue(always.isSatisfiedBy(-1))
        assertTrue(Always.isSatisfiedBy(null))
    }

    @Test
    fun `never holds for no candidate`() {
        val never: Specification<Int> = Never

        assertFalse(never.isSatisfiedBy(1))
        assertFalse(Never.isSatisfiedBy(null))
    }
}
