package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry
import pro.vlprojects.semantic.resilience.backoff.spring.boot.config.properties.BackoffConfigurationProperties

@AutoConfiguration
@EnableConfigurationProperties(BackoffConfigurationProperties::class)
public class BackoffAutoConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public fun backoffPropertiesRegistrar(properties: BackoffConfigurationProperties): BackoffRegistrar =
        BackoffPropertiesRegistrar(properties)

    @Bean
    @ConditionalOnMissingBean
    public fun backoffRegistry(
        registrars: ObjectProvider<BackoffRegistrar>,
        beanFactory: ListableBeanFactory,
    ): BackoffRegistry = BackoffRegistry()
        .also { registry ->
            registrars
                .orderedStream()
                .forEach { registrar -> registrar.register(registry) }
        }
        .also(BackoffBeansRegistrar(beanFactory)::register)
}
