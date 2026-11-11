package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.behavioral.resolver.ResolvingPolicy

class ResolvingPolicyTests {

    private val candidates = listOf("Good evening", "Evening", "Hi")

    @Test
    fun `the first policy selects the first candidate`() {
        assertEquals("Good evening", ResolvingPolicy.first<String>().select(candidates))
    }

    @Test
    fun `the last policy selects the last candidate`() {
        assertEquals("Hi", ResolvingPolicy.last<String>().select(candidates))
    }

    @Test
    fun `a policy is one instance for every type`() {
        assertSame(ResolvingPolicy.first<String>(), ResolvingPolicy.first<Int>())
        assertSame(ResolvingPolicy.last<String>(), ResolvingPolicy.last<Int>())
    }
}
