package pro.vlprojects.semantic.resilience.backoff.spring.boot

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import pro.vlprojects.semantic.resilience.backoff.Backoff

@AutoConfiguration
@EnableConfigurationProperties(BackoffProperties::class)
public class BackoffAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public fun backoffs(properties: BackoffProperties, beanFactory: ListableBeanFactory): Backoffs = beanFactory
        .getBeansOfType(Backoff::class.java)
        .let(BackoffsFactory(properties)::create)
}
