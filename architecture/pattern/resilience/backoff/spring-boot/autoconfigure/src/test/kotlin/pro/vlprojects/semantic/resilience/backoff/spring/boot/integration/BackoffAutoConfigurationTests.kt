package pro.vlprojects.semantic.resilience.backoff.spring.boot.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.annotation.ImportCandidates
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffAutoConfiguration
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import java.time.Duration

class BackoffAutoConfigurationTests {

    private val runner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(BackoffAutoConfiguration::class.java))

    @Test
    fun `the auto-configuration is registered`() {
        val names = ImportCandidates.load(AutoConfiguration::class.java, javaClass.classLoader).toList()

        assertTrue(BackoffAutoConfiguration::class.java.name in names)
    }

    @Test
    fun `the registry starts with no settings at all`() {
        runner.run { context ->
            assertNotNull(context.getRegistry().default)
        }
    }

    @Test
    fun `the application settings reach the instances`() {
        runner
            .withPropertyValues(
                "$PREFIX.default.type=exponential",
                "$PREFIX.default.initial-delay=2s",
                "$PREFIX.default.delay-limit=1m",
                "$PREFIX.default.multiplier=3",
                "$PREFIX.default.jitter=0",
                "$PREFIX.instances.payments.type=fixed",
                "$PREFIX.instances.payments.delay=5s",
                "$PREFIX.instances.orders.initial-delay=200ms",
            ).run { context ->
                val registry = context.getRegistry()

                assertEquals(Duration.ofSeconds(6), registry.default.calculate(2))
                assertEquals(Duration.ofSeconds(5), registry["payments"].calculate(2))
                assertEquals(Duration.ofMillis(600), registry["orders"].calculate(2))
            }
    }

    @Test
    fun `a backoff bean is more important than the settings`() {
        runner
            .withUserConfiguration(PaymentsBackoffConfiguration::class.java)
            .withPropertyValues("$PREFIX.instances.payments.initial-delay=1ms")
            .run { context ->
                assertSame(context.getBean("payments"), context.getRegistry()["payments"])
            }
    }

    @Test
    fun `an own BackoffRegistry bean turns the auto-configuration off`() {
        runner
            .withUserConfiguration(OwnBackoffsConfiguration::class.java)
            .run { context ->
                assertSame(context.getBean("ownRegistry"), context.getRegistry())
            }
    }

    @Test
    fun `an unknown field stops the start`() {
        runner
            .withPropertyValues("$PREFIX.instances.payments.delya=5s")
            .run { context -> assertTrue("delya" in context.readStartupFailureMessages()) }
    }

    @Test
    fun `an unknown type stops the start`() {
        runner
            .withPropertyValues("$PREFIX.default.type=linear")
            .run { context -> assertTrue("linear" in context.readStartupFailureMessages()) }
    }

    @Test
    fun `an invalid value stops the start and names the instance`() {
        runner
            .withPropertyValues("$PREFIX.instances.orders.jitter=1.5")
            .run { context -> assertTrue("'orders'" in context.readStartupFailureMessages()) }
    }

    private fun AssertableApplicationContext.getRegistry() = this.getBean(BackoffRegistry::class.java)

    private fun AssertableApplicationContext.readStartupFailureMessages() = this.startupFailure
        .let { failure -> generateSequence(failure, Throwable::cause) }
        .joinToString(separator = "\n") { failure -> failure.message.orEmpty() }

    @Configuration(proxyBeanMethods = false)
    class PaymentsBackoffConfiguration {

        @Bean
        fun payments(): Backoff = FixedBackoff.ofSeconds(9)
    }

    @Configuration(proxyBeanMethods = false)
    class OwnBackoffsConfiguration {

        @Bean
        fun ownRegistry() = BackoffRegistry(FixedBackoff.ofSeconds(1), emptyMap())
    }

    private companion object {

        const val PREFIX = "semantic.resilience.backoff"
    }
}
