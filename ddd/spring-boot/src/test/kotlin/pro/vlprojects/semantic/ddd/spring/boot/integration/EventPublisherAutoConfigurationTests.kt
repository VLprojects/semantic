package pro.vlprojects.semantic.ddd.spring.boot.integration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pro.vlprojects.semantic.ddd.architecture.event.EventPublisher
import pro.vlprojects.semantic.ddd.spring.boot.EventPublisherAutoConfiguration

class EventPublisherAutoConfigurationTests {

    @Test
    fun `pulled events reach the listeners of the application`() {
        val events = Cart(CartId(1)).pullEvents()

        applicationContext().use { context ->
            context.getBean<EventPublisher>().let { publisher -> events.forEach(publisher::publish) }

            assertEquals(events, context.getBean<Listener>().received)
        }
    }

    @Test
    fun `a publisher of the service takes the place of the default one`() {
        applicationContext(OwnPublisher::class.java).use { context ->
            context.getBean<EventPublisher>().let { assertSame(OwnPublisher.PUBLISHER, it) }
        }
    }

    // The auto-configuration goes last, as Spring Boot orders it after the beans of the service.
    private fun applicationContext(vararg configurations: Class<*>) = AnnotationConfigApplicationContext().apply {
        register(Listener::class.java, *configurations, EventPublisherAutoConfiguration::class.java)
        refresh()
    }

    @Configuration
    class OwnPublisher {

        @Bean
        fun publisher() = PUBLISHER

        companion object {

            val PUBLISHER = EventPublisher {}
        }
    }
}
