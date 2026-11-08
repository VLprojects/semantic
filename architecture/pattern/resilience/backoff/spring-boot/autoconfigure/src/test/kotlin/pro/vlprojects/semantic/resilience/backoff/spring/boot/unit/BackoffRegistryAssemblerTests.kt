package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffInstanceProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistryAssembler
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffType
import java.time.Duration

class BackoffRegistryAssemblerTests {

    @Test
    fun `without settings the default is exponential from a second to a minute`() {
        val default = assembleRegistry(default = BackoffInstanceProperties(jitter = 0.0)).default

        assertEquals(Duration.ofSeconds(1), default.calculate(1))
        assertEquals(Duration.ofSeconds(2), default.calculate(2))
        assertEquals(Duration.ofMinutes(1), default.calculate(100))
    }

    @Test
    fun `without settings the pause is jittered by a half`() {
        val delays = (1..200).map { assembleRegistry().default.calculate(1) }

        assertTrue(delays.all { it in Duration.ofMillis(500)..Duration.ofSeconds(1) })
        assertTrue(delays.distinct().size > 1)
    }

    @Test
    fun `an instance takes another type`() {
        val registry = assembleRegistry(
            default = BackoffInstanceProperties(jitter = 0.0),
            instances = mapOf(
                "payments" to BackoffInstanceProperties(type = BackoffType.FIXED, delay = Duration.ofSeconds(5)),
            ),
        )

        assertEquals(Duration.ofSeconds(5), registry["payments"].calculate(3))
    }

    @Test
    fun `the fields an instance leaves out come from the default`() {
        val registry = assembleRegistry(
            default = BackoffInstanceProperties(multiplier = 3.0, jitter = 0.0),
            instances = mapOf("orders" to BackoffInstanceProperties(initialDelay = Duration.ofMillis(200))),
        )

        assertEquals(Duration.ofMillis(200), registry["orders"].calculate(1))
        assertEquals(Duration.ofMillis(600), registry["orders"].calculate(2))
    }

    @Test
    fun `an instance of the other type inherits the fields of that type from the default`() {
        val registry = assembleRegistry(
            default = BackoffInstanceProperties(delay = Duration.ofSeconds(7), jitter = 0.0),
            instances = mapOf("payments" to BackoffInstanceProperties(type = BackoffType.FIXED)),
        )

        assertEquals(Duration.ofSeconds(7), registry["payments"].calculate(1))
    }

    @Test
    fun `the jitter wraps a fixed backoff too`() {
        val registry = assembleRegistry(
            instances = mapOf(
                "payments" to
                    BackoffInstanceProperties(type = BackoffType.FIXED, delay = Duration.ofSeconds(5), jitter = 1.0),
            ),
        )

        val delays = (1..200).map { registry["payments"].calculate(1) }

        assertTrue(delays.distinct().size > 1)
        assertTrue(delays.all { it <= Duration.ofSeconds(5) })
    }

    @Test
    fun `a bean with the name of an instance replaces its settings`() {
        val bean = FixedBackoff.ofSeconds(9)

        val registry = assembleRegistry(
            instances = mapOf("payments" to BackoffInstanceProperties(jitter = 2.0)),
            beans = mapOf("payments" to bean),
        )

        assertSame(bean, registry["payments"])
    }

    @Test
    fun `a bean named default replaces the default settings`() {
        val bean = FixedBackoff.ofSeconds(9)

        val registry = assembleRegistry(beans = mapOf("default" to bean))

        assertSame(bean, registry.default)
    }

    @Test
    fun `a field the type of an instance does not use fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            assembleRegistry(instances = mapOf("payments" to BackoffInstanceProperties(delay = Duration.ofSeconds(5))))
        }

        assertTrue("payments" in failure.message.orEmpty())
        assertTrue("delay" in failure.message.orEmpty())
    }

    @Test
    fun `the default block may hold the fields of both types`() {
        val settings =
            BackoffInstanceProperties(delay = Duration.ofSeconds(5), initialDelay = Duration.ofSeconds(2), jitter = 0.0)

        assertEquals(Duration.ofSeconds(2), assembleRegistry(default = settings).default.calculate(1))
    }

    @Test
    fun `an invalid value fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            assembleRegistry(instances = mapOf("orders" to BackoffInstanceProperties(jitter = 1.5)))
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("jitter" in failure.message.orEmpty())
    }

    @Test
    fun `an invalid default fails with its name`() {
        val failure = assertThrows<IllegalArgumentException> {
            assembleRegistry(default = BackoffInstanceProperties(multiplier = 0.5))
        }

        assertTrue("default" in failure.message.orEmpty())
    }

    @Test
    fun `the default is found by its name`() {
        val registry = assembleRegistry()

        assertSame(registry.default, registry["default"])
    }

    @Test
    fun `an instance named default fails`() {
        val failure = assertThrows<IllegalArgumentException> {
            assembleRegistry(instances = mapOf("default" to BackoffInstanceProperties()))
        }

        assertTrue("default" in failure.message.orEmpty())
    }

    private fun assembleRegistry(
        default: BackoffInstanceProperties = BackoffInstanceProperties(),
        instances: Map<String, BackoffInstanceProperties> = emptyMap(),
        beans: Map<String, Backoff> = emptyMap(),
    ) = BackoffRegistryAssembler(BackoffProperties(default, instances)).assemble(beans)
}
