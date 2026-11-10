package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.BackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.ExponentialBackoffFactory
import pro.vlprojects.semantic.resilience.backoff.spring.boot.factory.FixedBackoffFactory

@AutoConfiguration
public class BackoffAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public fun fixedBackoffFactory(): FixedBackoffFactory = FixedBackoffFactory()

    @Bean
    @ConditionalOnMissingBean
    public fun exponentialBackoffFactory(): ExponentialBackoffFactory = ExponentialBackoffFactory()

    @Bean
    @ConditionalOnMissingBean
    public fun backoffRegistry(
        environment: Environment,
        factories: List<BackoffFactory<*>>,
        backoffs: Map<String, Backoff>,
        customizers: ObjectProvider<BackoffRegistryCustomizer>,
    ): BackoffRegistry {
        val registry = BackoffRegistry()

        BackoffPropertiesRegistrar(environment, factories).registerBackoffs(registry)
        backoffs.forEach { (name, backoff) -> registry.replace(name, backoff) }
        customizers
            .orderedStream()
            .forEach { customizer -> customizer.customize(registry) }

        return registry
    }
}
