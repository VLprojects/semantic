package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleTypesException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.TypeNotFoundException

class AbstractPolymorphicBinderTests {

    @Test
    fun `the type is the first segment of a key`() {
        val delay = bind(mapOf("fixed.delay" to "1s"))

        assertEquals(FixedDelay::class, delay.type)
    }

    @Test
    fun `the fields come without the type segment`() {
        val delay = bind(mapOf("exponential.multiplier" to "2", "exponential.initial-delay" to "1s"))

        assertEquals(mapOf("multiplier" to "2", "initial-delay" to "1s"), delay.sources.first())
    }

    @Test
    fun `the name is the path with the type segment as in the block`() {
        val delay = bind(mapOf("Fixed.delay" to "1s"))

        assertEquals("orders.Fixed", delay.name)
    }

    @Test
    fun `a key with no field names the type`() {
        val delay = bind(mapOf("fixed" to ""))

        assertEquals(FixedDelay::class, delay.type)
        assertEquals(emptyMap<String, String>(), delay.sources.first())
    }

    @Test
    fun `two spellings of one type are one type`() {
        val delay = bind(mapOf("fixed.delay" to "1s", "Fixed.jitter" to "0.1"))

        assertEquals(mapOf("delay" to "1s", "jitter" to "0.1"), delay.sources.first())
    }

    @Test
    fun `several types in a block are rejected with the path and the types`() {
        val failure = assertThrows<MultipleTypesException> {
            bind(mapOf("fixed.delay" to "1s", "exponential.multiplier" to "2"))
        }

        assertEquals("orders", failure.name)
        assertEquals(setOf("fixed", "exponential"), failure.typeNames)
    }

    @Test
    fun `an unknown type is rejected with the known ones`() {
        val failure = assertThrows<TypeNotFoundException> {
            bind(mapOf("exponental.multiplier" to "2"))
        }

        assertEquals("orders", failure.name)
        assertEquals("exponental", failure.typeName)
        assertEquals(setOf("exponential", "fixed"), failure.knownTypeNames)
    }

    @Test
    fun `a field outside a type names an unknown type`() {
        val failure = assertThrows<TypeNotFoundException> {
            bind(mapOf("fixed.delay" to "1s", "multiplier" to "2"))
        }

        assertEquals("orders", failure.name)
        assertEquals("multiplier", failure.typeName)
    }

    @Test
    fun `an empty block takes the type of the defaults`() {
        val delay = bind(emptyMap(), mapOf("fixed.delay" to "1s"))

        assertEquals(FixedDelay::class, delay.type)
        assertEquals("orders.fixed", delay.name)
        assertEquals(listOf(emptyMap(), mapOf("delay" to "1s")), delay.sources)
    }

    @Test
    fun `an empty block rejects defaults of several types`() {
        val failure = assertThrows<MultipleTypesException> {
            bind(emptyMap(), mapOf("fixed.delay" to "1s", "exponential.multiplier" to "2"))
        }

        assertEquals("orders", failure.name)
    }

    @Test
    fun `an empty block with no defaults takes the default type`() {
        val delay = bind(emptyMap(), binder = DelayBinder("fixed"))

        assertEquals(FixedDelay::class, delay.type)
        assertEquals("orders.fixed", delay.name)
        assertEquals(listOf(emptyMap<String, String>(), emptyMap()), delay.sources)
    }

    @Test
    fun `an unknown default type is rejected`() {
        val failure = assertThrows<TypeNotFoundException> {
            bind(emptyMap(), binder = DelayBinder("linear"))
        }

        assertEquals("orders", failure.name)
        assertEquals("linear", failure.typeName)
    }

    @Test
    fun `the defaults of another type are left out`() {
        val delay = bind(
            mapOf("fixed.delay" to "1s"),
            mapOf("exponential.multiplier" to "2", "linear.step" to "1", "fixed.jitter" to "0.1"),
        )

        assertEquals(listOf(mapOf("delay" to "1s"), mapOf("jitter" to "0.1")), delay.sources)
    }

    @Test
    fun `the defaults spelt otherwise are of the same type`() {
        val delay = bind(mapOf("Fixed.delay" to "1s"), mapOf("fixed.jitter" to "0.1"))

        assertEquals(mapOf("jitter" to "0.1"), delay.sources.last())
    }

    @Test
    fun `the block comes before the defaults`() {
        val delay = bind(mapOf("fixed.delay" to "1s"), mapOf("fixed.delay" to "5s"))

        assertEquals(listOf(mapOf("delay" to "1s"), mapOf("delay" to "5s")), delay.sources)
    }

    private fun bind(
        properties: Map<String, String>,
        defaults: Map<String, String> = emptyMap(),
        binder: DelayBinder = DelayBinder(),
    ) = binder
        .bind("orders", properties, defaults)
        .let { delay -> assertInstanceOf(BoundDelay::class.java, delay) }
}
