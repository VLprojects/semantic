package pro.vlprojects.semantic.ddd.spring.boot

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import pro.vlprojects.semantic.ddd.architecture.event.EventPublisher

@AutoConfiguration
public class EventPublisherAutoConfiguration {

    // The same publisher @DomainEvents hands events to, so listeners and the Modulith registry see one stream.
    @Bean
    @ConditionalOnMissingBean
    public fun eventPublisher(publisher: ApplicationEventPublisher): EventPublisher = EventPublisher(
        publisher::publishEvent,
    )
}
