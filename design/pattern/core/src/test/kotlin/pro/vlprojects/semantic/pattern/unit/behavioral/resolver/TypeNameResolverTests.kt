package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.TypeNameResolver

class TypeNameResolverTests {

    private val resolver = TypeNameResolver.of(Backoff::class, listOf(FixedBackoff::class, FixedDelayBackoff::class))

    @Test
    fun `a subtype is named by its class without the name of the base type`() {
        assertEquals(setOf("fixed", "fixed-delay"), resolver.typeNames)
        assertEquals(FixedBackoff::class, resolver.resolve("fixed"))
        assertEquals(FixedDelayBackoff::class, resolver.resolve("fixed-delay"))
    }

    @Test
    fun `a type name is matched regardless of case and separators`() {
        assertEquals(FixedDelayBackoff::class, resolver.resolve("fixedDelay"))
        assertEquals(FixedDelayBackoff::class, resolver.resolve("FixedDelay"))
        assertEquals(FixedDelayBackoff::class, resolver.resolve("fixed_delay"))
        assertEquals(FixedBackoff::class, resolver.resolve("FIXED"))
    }

    @Test
    fun `an unknown type name resolves nothing`() {
        assertNull(resolver.resolveOrNull("exponential"))
    }

    @Test
    fun `a type is named as it is given`() {
        val named = TypeNameResolver.of(mapOf("constant" to FixedBackoff::class))

        assertEquals(setOf("constant"), named.typeNames)
        assertEquals(FixedBackoff::class, named.resolve("Constant"))
    }

    @Test
    fun `two types of one name are rejected`() {
        assertThrows<IllegalArgumentException> {
            TypeNameResolver.of(Backoff::class, listOf(FixedBackoff::class, Fixed::class))
        }
        assertThrows<IllegalArgumentException> {
            TypeNameResolver.of(mapOf("fixed-delay" to FixedBackoff::class, "fixedDelay" to FixedDelayBackoff::class))
        }
    }
}
