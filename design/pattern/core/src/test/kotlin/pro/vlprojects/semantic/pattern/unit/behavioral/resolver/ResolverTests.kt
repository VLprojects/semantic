package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import pro.vlprojects.semantic.pattern.behavioral.resolver.ResolvingPolicy
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.MultipleResolvedException
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.NotResolvedException

class ResolverTests {

    private val greetings = mapOf(
        "morning" to listOf("Good morning"),
        "evening" to listOf("Good evening", "Evening"),
    )

    private val resolver = Resolver<String, String> { time -> greetings[time].orEmpty() }

    private val failing = ResolvingPolicy<String> { candidates -> error("$candidates are selected") }

    @Test
    fun `every value of a context is resolved`() {
        assertEquals(listOf("Good evening", "Evening"), resolver.resolveAll("evening"))
        assertEquals(emptyList<String>(), resolver.resolveAll("night"))
    }

    @Test
    fun `a single value is resolved without a policy`() {
        assertEquals("Good morning", resolver.resolve("morning"))
        assertEquals("Good morning", resolver.resolveOrNull("morning"))
    }

    @Test
    fun `a single value is resolved without asking the policy`() {
        assertEquals("Good morning", resolver.resolve("morning", failing))
        assertEquals("Good morning", resolver.resolveOrNull("morning", failing))
    }

    @Test
    fun `nothing resolved is null or a failure`() {
        assertNull(resolver.resolveOrNull("night"))
        assertNull(resolver.resolveOrNull("night", failing))
        assertEquals("night", assertThrows<NotResolvedException> { resolver.resolve("night") }.context)
        assertEquals("night", assertThrows<NotResolvedException> { resolver.resolve("night", failing) }.context)
    }

    @Test
    fun `several values without a policy are a failure`() {
        val failure = assertThrows<MultipleResolvedException> { resolver.resolve("evening") }

        assertEquals("evening", failure.context)
        assertEquals(listOf("Good evening", "Evening"), failure.candidates)
        assertThrows<MultipleResolvedException> { resolver.resolveOrNull("evening") }
    }

    @Test
    fun `several values are selected by the policy`() {
        assertEquals("Evening", resolver.resolve("evening", ResolvingPolicy.last()))
        assertEquals("Good evening", resolver.resolveOrNull("evening", ResolvingPolicy.first()))
    }
}
