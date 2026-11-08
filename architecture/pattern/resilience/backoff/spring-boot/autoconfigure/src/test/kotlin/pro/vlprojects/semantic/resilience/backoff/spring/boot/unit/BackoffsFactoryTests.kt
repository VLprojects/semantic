package pro.vlprojects.semantic.resilience.backoff.spring.boot.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffProperties
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffSettings
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffType
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffsFactory
import java.time.Duration

class BackoffsFactoryTests {

    @Test
    fun `without settings the default is exponential from a second to a minute`() {
        val default = createBackoffs(default = BackoffSettings(jitter = 0.0)).default

        assertEquals(Duration.ofSeconds(1), default.calculate(1))
        assertEquals(Duration.ofSeconds(2), default.calculate(2))
        assertEquals(Duration.ofMinutes(1), default.calculate(100))
    }

    @Test
    fun `without settings the pause is jittered by a half`() {
        val delays = (1..200).map { createBackoffs().default.calculate(1) }

        assertTrue(delays.all { it in Duration.ofMillis(500)..Duration.ofSeconds(1) })
        assertTrue(delays.distinct().size > 1)
    }

    @Test
    fun `an instance takes another type`() {
        val backoffs = createBackoffs(
            default = BackoffSettings(jitter = 0.0),
            instances = mapOf("payments" to BackoffSettings(type = BackoffType.FIXED, delay = Duration.ofSeconds(5))),
        )

        assertEquals(Duration.ofSeconds(5), backoffs["payments"].calculate(3))
    }

    @Test
    fun `the fields an instance leaves out come from the default`() {
        val backoffs = createBackoffs(
            default = BackoffSettings(multiplier = 3.0, jitter = 0.0),
            instances = mapOf("orders" to BackoffSettings(initialDelay = Duration.ofMillis(200))),
        )

        assertEquals(Duration.ofMillis(200), backoffs["orders"].calculate(1))
        assertEquals(Duration.ofMillis(600), backoffs["orders"].calculate(2))
    }

    @Test
    fun `an instance of the other type inherits the fields of that type from the default`() {
        val backoffs = createBackoffs(
            default = BackoffSettings(delay = Duration.ofSeconds(7), jitter = 0.0),
            instances = mapOf("payments" to BackoffSettings(type = BackoffType.FIXED)),
        )

        assertEquals(Duration.ofSeconds(7), backoffs["payments"].calculate(1))
    }

    @Test
    fun `the jitter wraps a fixed backoff too`() {
        val backoffs = createBackoffs(
            instances = mapOf(
                "payments" to BackoffSettings(type = BackoffType.FIXED, delay = Duration.ofSeconds(5), jitter = 1.0),
            ),
        )

        val delays = (1..200).map { backoffs["payments"].calculate(1) }

        assertTrue(delays.distinct().size > 1)
        assertTrue(delays.all { it <= Duration.ofSeconds(5) })
    }

    @Test
    fun `a bean with the name of an instance replaces its settings`() {
        val bean = FixedBackoff.ofSeconds(9)

        val backoffs = createBackoffs(
            instances = mapOf("payments" to BackoffSettings(jitter = 2.0)),
            beans = mapOf("payments" to bean),
        )

        assertSame(bean, backoffs["payments"])
    }

    @Test
    fun `a bean named default replaces the default settings`() {
        val bean = FixedBackoff.ofSeconds(9)

        val backoffs = createBackoffs(beans = mapOf("default" to bean))

        assertSame(bean, backoffs.default)
    }

    @Test
    fun `a field the type of an instance does not use fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoffs(instances = mapOf("payments" to BackoffSettings(delay = Duration.ofSeconds(5))))
        }

        assertTrue("payments" in failure.message.orEmpty())
        assertTrue("delay" in failure.message.orEmpty())
    }

    @Test
    fun `the default block may hold the fields of both types`() {
        val settings =
            BackoffSettings(delay = Duration.ofSeconds(5), initialDelay = Duration.ofSeconds(2), jitter = 0.0)

        assertEquals(Duration.ofSeconds(2), createBackoffs(default = settings).default.calculate(1))
    }

    @Test
    fun `an invalid value fails with the name of the instance`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoffs(instances = mapOf("orders" to BackoffSettings(jitter = 1.5)))
        }

        assertTrue("orders" in failure.message.orEmpty())
        assertTrue("jitter" in failure.message.orEmpty())
    }

    @Test
    fun `an invalid default fails with its name`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoffs(default = BackoffSettings(multiplier = 0.5))
        }

        assertTrue("default" in failure.message.orEmpty())
    }

    @Test
    fun `the default is found by its name`() {
        val backoffs = createBackoffs()

        assertSame(backoffs.default, backoffs["default"])
    }

    @Test
    fun `an instance named default fails`() {
        val failure = assertThrows<IllegalArgumentException> {
            createBackoffs(instances = mapOf("default" to BackoffSettings()))
        }

        assertTrue("default" in failure.message.orEmpty())
    }

    private fun createBackoffs(
        default: BackoffSettings = BackoffSettings(),
        instances: Map<String, BackoffSettings> = emptyMap(),
        beans: Map<String, Backoff> = emptyMap(),
    ) = BackoffsFactory(BackoffProperties(default, instances)).create(beans)
}
