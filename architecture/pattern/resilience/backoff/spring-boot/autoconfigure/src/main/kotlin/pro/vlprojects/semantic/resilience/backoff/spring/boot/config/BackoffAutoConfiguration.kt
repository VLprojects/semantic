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
    @ConditionalOnMissingBean
    public fun backoffRegistry(
        properties: BackoffConfigurationProperties,
        backoffs: Map<String, Backoff>,
        customizers: ObjectProvider<BackoffRegistryCustomizer>,
    ): BackoffRegistry = BackoffPropertiesMapper(properties)
        .asBackoffs()
        .plus(backoffs)
        .let(::BackoffRegistry)
        .also { registry ->
            customizers
                .orderedStream()
                .forEach { customizer -> customizer.customize(registry) }
        }
}
