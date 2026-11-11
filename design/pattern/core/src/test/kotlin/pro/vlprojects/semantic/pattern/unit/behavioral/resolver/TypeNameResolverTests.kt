package pro.vlprojects.semantic.pattern.unit.behavioral.resolver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.TypeNameResolver
import kotlin.reflect.KClass

class TypeNameResolverTests {

    private val resolver = TypeNameResolver.of(Backoff::class, listOf(FixedBackoff::class, FixedDelayBackoff::class))

    @Test
    fun `a subtype is named by its class without the name of the base type`() {
        assertEquals(setOf("fixed", "fixed-delay"), resolver.typeNames)
        assertEquals(FixedBackoff::class, resolver.resolve("fixed"))
        assertEquals(FixedDelayBackoff::class, resolver.resolve("fixed-delay"))
    }

    @Test
    fun `a subtype named as its base type keeps its whole name`() {
        val backoffResolver = TypeNameResolver.of(Backoff::class, listOf(Backoff::class))

        assertEquals(setOf("backoff"), backoffResolver.typeNames)
    }

    @Test
    fun `a type name is matched regardless of case, dashes and underscores`() {
        assertEquals(FixedDelayBackoff::class, resolver.resolve("fixedDelay"))
        assertEquals(FixedDelayBackoff::class, resolver.resolve("FixedDelay"))
        assertEquals(FixedDelayBackoff::class, resolver.resolve("FIXED_DELAY"))
        assertEquals(FixedBackoff::class, resolver.resolve("FIXED"))
    }

    @Test
    fun `an unknown type name resolves nothing`() {
        assertNull(resolver.resolveOrNull("exponential"))
    }

    @Test
    fun `a dot in a type name is significant`() {
        assertNull(resolver.resolveOrNull("fixed.delay"))
    }

    @Test
    fun `an empty type name resolves nothing`() {
        assertNull(resolver.resolveOrNull(""))
    }

    @Test
    fun `a type name of separators only resolves nothing`() {
        assertNull(resolver.resolveOrNull("--"))
    }

    @Test
    fun `a type is named as it is given`() {
        val mapResolver = TypeNameResolver.of(mapOf("constant" to FixedBackoff::class))

        assertEquals(setOf("constant"), mapResolver.typeNames)
        assertEquals(FixedBackoff::class, mapResolver.resolve("constant"))
    }

    @Test
    fun `a change of the source map does not reach the resolver`() {
        val types = mutableMapOf<String, KClass<out Backoff>>("fixed" to FixedBackoff::class)
        val mapResolver = TypeNameResolver.of(types)

        types["fixed-delay"] = FixedDelayBackoff::class

        assertEquals(setOf("fixed"), mapResolver.typeNames)
        assertNull(mapResolver.resolveOrNull("fixed-delay"))
    }

    @Test
    fun `a repeated subtype is named once`() {
        val fixedResolver = TypeNameResolver.of(Backoff::class, listOf(FixedBackoff::class, FixedBackoff::class))

        assertEquals(setOf("fixed"), fixedResolver.typeNames)
    }

    @Test
    fun `two subtypes of one name are rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            TypeNameResolver.of(Backoff::class, listOf(FixedBackoff::class, Fixed::class))
        }

        assertEquals(
            "Several subtypes have the name 'fixed': ${FixedBackoff::class.java.name}, ${Fixed::class.java.name}",
            failure.message,
        )
    }

    @Test
    fun `two given names that differ in case and separators only are rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            TypeNameResolver.of(mapOf("fixed-delay" to FixedBackoff::class, "fixedDelay" to FixedDelayBackoff::class))
        }

        assertEquals(
            "Type names coincide regardless of case, dashes and underscores: " +
                "'fixed-delay' of ${FixedBackoff::class.java.name}, " +
                "'fixedDelay' of ${FixedDelayBackoff::class.java.name}",
            failure.message,
        )
    }

    @Test
    fun `an anonymous subtype is rejected`() {
        val anonymous = object : Backoff {}

        val failure = assertThrows<IllegalArgumentException> {
            TypeNameResolver.of(Backoff::class, listOf(anonymous::class))
        }

        assertEquals("A class without a name has no type name: ${anonymous::class.java.name}", failure.message)
    }

    @Test
    fun `a given name of separators only is rejected`() {
        val failure = assertThrows<IllegalArgumentException> {
            TypeNameResolver.of(mapOf("-" to FixedBackoff::class))
        }

        assertEquals("A type name is empty or has dashes and underscores only: '-'", failure.message)
    }
}
