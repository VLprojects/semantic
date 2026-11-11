package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.PredicateResolver
import pro.vlprojects.semantic.pattern.behavioral.resolver.ResolvingPolicy
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.MultipleResolvedException

class PredicateResolverTests {

    private val greetings = mutableListOf("Good morning", "Good evening", "Evening")

    private val resolver = PredicateResolver<String, String>(greetings) { greeting, time -> time in greeting }

    @Test
    fun `the values that hold for a context are resolved in their order`() {
        assertEquals(listOf("Good evening", "Evening"), resolver.resolveAll("vening"))
    }

    @Test
    fun `a single value that holds is resolved`() {
        assertEquals("Good morning", resolver.resolve("morning"))
    }

    @Test
    fun `no value that holds resolves nothing`() {
        assertNull(resolver.resolveOrNull("night"))
    }

    @Test
    fun `several values that hold are selected by the policy`() {
        assertThrows<MultipleResolvedException> { resolver.resolve("vening") }
        assertEquals("Good evening", resolver.resolve("vening", ResolvingPolicy.first()))
        assertEquals("Evening", resolver.resolve("vening", ResolvingPolicy.last()))
    }

    @Test
    fun `a change of the source list does not reach the resolver`() {
        greetings += "Good night"

        assertNull(resolver.resolveOrNull("night"))
    }
}
