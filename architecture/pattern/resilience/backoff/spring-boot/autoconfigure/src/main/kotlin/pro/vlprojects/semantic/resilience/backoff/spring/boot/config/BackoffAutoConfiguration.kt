package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties

@AutoConfiguration
@EnableConfigurationProperties(BackoffConfigurationProperties::class)
public class BackoffAutoConfiguration {

    @Bean
    public fun standardBackoffRegistryCustomizer(
        properties: BackoffConfigurationProperties,
        backoffs: Map<String, Backoff>,
    ): BackoffRegistryCustomizer = StandardBackoffRegistryCustomizer(properties, backoffs)

    @Bean
    @ConditionalOnMissingBean
    public fun backoffRegistry(
        customizers: ObjectProvider<BackoffRegistryCustomizer>,
    ): BackoffRegistry = BackoffRegistry()
        .also { registry ->
            customizers
                .orderedStream()
                .forEach { customizer -> customizer.customize(registry) }
        }
}
