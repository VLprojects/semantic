package pro.vlprojects.semantic.resilience.backoff.spring.boot.integration.config

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
import org.springframework.core.annotation.Order
import pro.vlprojects.semantic.resilience.backoff.FixedBackoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.DEFAULT_BACKOFF_NAME
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffAutoConfiguration
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.BackoffRegistryCustomizer
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
    fun `a backoff bean named default replaces the default block`() {
        runner
            .withUserConfiguration(DefaultBackoffConfiguration::class.java)
            .run { context ->
                assertSame(context.getBean("default"), context.getRegistry().default)
            }
    }

    @Test
    fun `an invalid setting stops the start even when a bean replaces it`() {
        runner
            .withUserConfiguration(PaymentsBackoffConfiguration::class.java)
            .withPropertyValues("$PREFIX.instances.payments.jitter=1.5")
            .run { context -> assertTrue("'payments'" in context.readStartupFailureMessages()) }
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
    fun `a customizer adds to the registry`() {
        runner
            .withUserConfiguration(AddingCustomizerConfiguration::class.java)
            .run { context ->
                assertEquals(Duration.ofSeconds(9), context.getRegistry()["orders"].calculate(1))
            }
    }

    @Test
    fun `a customizer sees the settings and the backoff beans`() {
        runner
            .withUserConfiguration(
                PaymentsBackoffConfiguration::class.java,
                ReplacingCustomizerConfiguration::class.java,
            )
            .withPropertyValues("$PREFIX.instances.orders.initial-delay=1ms")
            .run { context ->
                val registry = context.getRegistry()

                assertEquals(setOf("default", "orders", "payments"), registry.names)
                assertTrue(registry.names.all { name -> registry[name].calculate(1) == CUSTOMIZED_DELAY })
            }
    }

    @Test
    fun `customizers run in their order`() {
        runner
            .withUserConfiguration(OrderedCustomizersConfiguration::class.java)
            .run { context ->
                assertEquals(Duration.ofSeconds(2), context.getRegistry().default.calculate(1))
            }
    }

    @Test
    fun `a backoff bean that needs the registry stops the start`() {
        runner
            .withUserConfiguration(CircularBackoffConfiguration::class.java)
            .run { context -> assertNotNull(context.startupFailure) }
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
        fun payments() = FixedBackoff.ofSeconds(9)
    }

    @Configuration(proxyBeanMethods = false)
    class DefaultBackoffConfiguration {

        @Bean
        fun default() = FixedBackoff.ofSeconds(9)
    }

    @Configuration(proxyBeanMethods = false)
    class OwnBackoffsConfiguration {

        @Bean
        fun ownRegistry() = BackoffRegistry()
    }

    @Configuration(proxyBeanMethods = false)
    class AddingCustomizerConfiguration {

        @Bean
        fun addingCustomizer() = BackoffRegistryCustomizer { registry ->
            registry.register("orders", FixedBackoff.ofSeconds(9))
        }
    }

    @Configuration(proxyBeanMethods = false)
    class ReplacingCustomizerConfiguration {

        @Bean
        fun replacingCustomizer() = BackoffRegistryCustomizer { registry ->
            registry.names.forEach { name -> registry.replace(name, FixedBackoff.of(CUSTOMIZED_DELAY)) }
        }
    }

    @Configuration(proxyBeanMethods = false)
    class OrderedCustomizersConfiguration {

        @Bean
        @Order(2)
        fun lastCustomizer() = BackoffRegistryCustomizer { registry ->
            registry.replace(DEFAULT_BACKOFF_NAME, FixedBackoff.ofSeconds(2))
        }

        @Bean
        @Order(1)
        fun firstCustomizer() = BackoffRegistryCustomizer { registry ->
            registry.replace(DEFAULT_BACKOFF_NAME, FixedBackoff.ofSeconds(1))
        }
    }

    @Configuration(proxyBeanMethods = false)
    class CircularBackoffConfiguration {

        @Bean
        fun payments(registry: BackoffRegistry) = registry.default
    }

    private companion object {

        const val PREFIX = "semantic.resilience.backoff"
        val CUSTOMIZED_DELAY = Duration.ofSeconds(3)
    }
}
