package pro.vlprojects.semantic.pattern.unit.enterprise.plugin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.pattern.behavioral.resolver.Resolver
import pro.vlprojects.semantic.pattern.behavioral.resolver.error.MultipleResolvedException
import pro.vlprojects.semantic.pattern.enterprise.plugin.DefaultPolymorphicConfigurationBinder
import pro.vlprojects.semantic.pattern.enterprise.plugin.PropertiesDecoder
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.InvalidConfigurationPropertyException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.MultipleConfigurationsException
import pro.vlprojects.semantic.pattern.enterprise.plugin.error.UnknownConfigurationException
import kotlin.reflect.KClass

class DefaultPolymorphicConfigurationBinderTests {

    @Test
    fun `the type is the first segment of a key`() {
        val delay = bind(mapOf("fixed.delay" to "1s"))

        assertEquals(FixedDelay::class, delay::class)
    }

    @Test
    fun `the fields come without the type segment`() {
        val delay = bind(mapOf("exponential.multiplier" to "2", "exponential.initial-delay" to "1s"))

        assertEquals(mapOf("multiplier" to "2", "initial-delay" to "1s"), delay.properties)
    }

    @Test
    fun `a key with no field names the type`() {
        val delay = bind(mapOf("fixed" to ""))

        assertEquals(FixedDelay::class, delay::class)
        assertEquals(emptyMap<String, String>(), delay.properties)
    }

    @Test
    fun `an empty value on the block key is an empty block`() {
        val delay = bind(mapOf("" to ""))

        assertEquals(ExponentialDelay::class, delay::class)
        assertEquals(emptyMap<String, String>(), delay.properties)
    }

    @Test
    fun `a value on the block key is rejected as unbound`() {
        assertUnbound(mapOf("" to "5"), "['orders']")
    }

    @Test
    fun `a list in place of the block is rejected as unbound`() {
        assertUnbound(mapOf("[0]" to "fixed", "[1]" to ""), "['orders[0]', 'orders[1]']")
    }

    @Test
    fun `a value on the type key is rejected as unbound`() {
        assertUnbound(mapOf("fixed" to "5"), "['orders.fixed']")
    }

    @Test
    fun `a list in place of the type is rejected as unbound`() {
        assertUnbound(mapOf("fixed[0]" to "", "fixed[1].delay" to "2s"), "['orders.fixed[0]', 'orders.fixed[1].delay']")
    }

    @Test
    fun `a field with no type is rejected as unbound`() {
        assertUnbound(mapOf(".delay" to "1s"), "['orders..delay']")
    }

    @Test
    fun `an empty value on the type key is dropped and the fields stay`() {
        val delay = bind(mapOf("fixed" to "", "fixed.delay" to "1s"))

        assertEquals(mapOf("delay" to "1s"), delay.properties)
    }

    @Test
    fun `a nested field keeps its path`() {
        val delay = bind(mapOf("exponential.jitter.factor" to "0.1"))

        assertEquals(mapOf("jitter.factor" to "0.1"), delay.properties)
    }

    @Test
    fun `an indexed field keeps its index`() {
        val delay = bind(mapOf("exponential.retry-on-statuses[0]" to "503"))

        assertEquals(mapOf("retry-on-statuses[0]" to "503"), delay.properties)
    }

    @Test
    fun `two spellings of one type are one type`() {
        val delay = bind(mapOf("fixed.delay" to "1s", "Fixed.jitter" to "0.1"))

        assertEquals(mapOf("delay" to "1s", "jitter" to "0.1"), delay.properties)
    }

    @Test
    fun `a field under two spellings of the type keeps the later key`() {
        val delay = bind(mapOf("fixed.delay" to "1s", "Fixed.delay" to "2s"))

        assertEquals(mapOf("delay" to "2s"), delay.properties)
    }

    @Test
    fun `the prefix of an error takes the first spelling of the type`() {
        val binder = createRejectingBinder()

        val failure = assertThrows<InvalidConfigurationPropertyException> {
            bind(mapOf("fixed.delay" to "1s", "Fixed.jitter" to "0.1"), binder)
        }

        assertEquals("orders.fixed", failure.prefix)
    }

    @Test
    fun `several types in a block are rejected with the prefix and the types`() {
        val failure = assertThrows<MultipleConfigurationsException> {
            bind(mapOf("fixed.delay" to "1s", "exponential.multiplier" to "2"))
        }

        assertEquals("orders", failure.prefix)
        assertEquals(setOf("fixed", "exponential"), failure.typeNames)
    }

    @Test
    fun `several types are named by their first spellings`() {
        val failure = assertThrows<MultipleConfigurationsException> {
            bind(mapOf("fixed.delay" to "1s", "Fixed.jitter" to "0.1", "exponential.multiplier" to "2"))
        }

        assertEquals(setOf("fixed", "exponential"), failure.typeNames)
    }

    @Test
    fun `an unknown type is rejected with the prefix`() {
        val failure = assertThrows<UnknownConfigurationException> {
            bind(mapOf("exponental.multiplier" to "2"))
        }

        assertEquals("orders", failure.prefix)
        assertEquals("exponental", failure.typeName)
    }

    @Test
    fun `an unknown type is rejected before several types`() {
        val failure = assertThrows<UnknownConfigurationException> {
            bind(mapOf("fixed.delay" to "1s", "exponental.multiplier" to "2"))
        }

        assertEquals("exponental", failure.typeName)
    }

    @Test
    fun `an unknown type is rejected before unbound keys`() {
        val failure = assertThrows<UnknownConfigurationException> {
            bind(mapOf("[0]" to "fixed", "exponental.multiplier" to "2"))
        }

        assertEquals("exponental", failure.typeName)
    }

    @Test
    fun `several types are rejected before unbound keys`() {
        assertThrows<MultipleConfigurationsException> {
            bind(mapOf("fixed" to "5", "exponential.multiplier" to "2"))
        }
    }

    @Test
    fun `a field outside a type names an unknown type`() {
        val failure = assertThrows<UnknownConfigurationException> {
            bind(mapOf("fixed.delay" to "1s", "multiplier" to "2"))
        }

        assertEquals("orders", failure.prefix)
        assertEquals("multiplier", failure.typeName)
    }

    @Test
    fun `an empty block takes the default type`() {
        val delay = bind(emptyMap(), createBinder(defaultTypeName = "fixed"))

        assertEquals(FixedDelay::class, delay::class)
        assertEquals(emptyMap<String, String>(), delay.properties)
    }

    @Test
    fun `an empty block puts the default type into the prefix of an error`() {
        val binder = createRejectingBinder()

        val failure = assertThrows<InvalidConfigurationPropertyException> { bind(emptyMap(), binder) }

        assertEquals("orders.exponential", failure.prefix)
    }

    @Test
    fun `fields that do not fit are rejected with the prefix of the type and the error of the decoder`() {
        val cause = IllegalStateException("No field 'delay' in a fixed delay")
        val binder = createRejectingBinder(cause)

        val failure = assertThrows<InvalidConfigurationPropertyException> {
            bind(mapOf("Fixed.delay" to "1s"), binder)
        }

        assertEquals("orders.Fixed", failure.prefix)
        assertSame(cause, failure.cause)
    }

    @Test
    fun `an error of a nested binder is wrapped with the prefix of the type`() {
        val cause = UnknownConfigurationException("orders.fixed.jitter", "gaussian")
        val binder = createRejectingBinder(cause)

        val failure = assertThrows<InvalidConfigurationPropertyException> {
            bind(mapOf("fixed.jitter.gaussian.sigma" to "0.1"), binder)
        }

        assertEquals("orders.fixed", failure.prefix)
        assertSame(cause, failure.cause)
    }

    @Test
    fun `an empty prefix gives the type segment alone`() {
        val binder = createRejectingBinder()

        val failure = assertThrows<InvalidConfigurationPropertyException> {
            binder.bind("", mapOf("fixed.delay" to "1s"))
        }

        assertEquals("fixed", failure.prefix)
    }

    @Test
    fun `an error that is not an exception passes through`() {
        val error = StackOverflowError()
        val binder = createRejectingBinder(error)

        val failure = assertThrows<StackOverflowError> { bind(mapOf("fixed.delay" to "1s"), binder) }

        assertSame(error, failure)
    }

    @Test
    fun `an unknown default type stops the binder from being made`() {
        val failure = assertThrows<IllegalArgumentException> { createBinder(defaultTypeName = "linear") }

        assertEquals("The default type 'linear' is unknown to the resolver", failure.message)
    }

    @Test
    fun `a type of several classes is rejected by the resolver`() {
        val binder = createBinder(resolver = ambiguousDelayTypeResolver)

        assertThrows<MultipleResolvedException> { bind(mapOf("fixed.delay" to "1s"), binder) }
    }

    @Test
    fun `a default type of several classes stops the binder from being made`() {
        assertThrows<MultipleResolvedException> {
            createBinder(defaultTypeName = "fixed", resolver = ambiguousDelayTypeResolver)
        }
    }

    private fun assertUnbound(properties: Map<String, String>, expectedKeys: String) {
        val failure = assertThrows<InvalidConfigurationPropertyException> { bind(properties) }

        assertEquals("orders", failure.prefix)
        assertEquals("Unbound configuration properties: $expectedKeys", failure.cause?.message)
    }

    private fun createRejectingBinder(failure: Throwable = IllegalStateException()) = failure
        .let(::RejectingDecoder)
        .let { decoder -> createBinder(decoder = decoder) }

    private fun bind(
        properties: Map<String, String>,
        binder: DefaultPolymorphicConfigurationBinder<Delay> = createBinder(),
    ) = binder.bind("orders", properties)

    private fun createBinder(
        defaultTypeName: String = "exponential",
        resolver: Resolver<String, KClass<out Delay>> = delayTypeResolver,
        decoder: PropertiesDecoder = MapConstructorDecoder,
    ) = DefaultPolymorphicConfigurationBinder(resolver, decoder, defaultTypeName)
}
