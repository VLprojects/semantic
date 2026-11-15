package pro.vlprojects.semantic.pattern.integration.structural.decorator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.config.BeanDefinitionCustomizer
import org.springframework.beans.factory.support.AbstractBeanDefinition
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import pro.vlprojects.semantic.pattern.structural.decorator.DecoratedAnnotationBeanPostProcessor
import pro.vlprojects.semantic.pattern.structural.decorator.DecoratorFactory
import pro.vlprojects.semantic.pattern.structural.decorator.DecoratorFactoryBeanPostProcessor
import java.util.function.Supplier

class DecoratorFactoryBeanPostProcessorTests {

    @Test
    fun `factories decorate a bean of their type with the first one outermost`() {
        this.start(Caption::class.java).use { context ->
            assertEquals("#caption↕#", context.getBean(VisualComponent::class.java).draw())
        }
    }

    @Test
    fun `factories of classes and of bean methods share one order`() {
        this.start(Caption::class.java, PipeBorderDecoratorFactory::class.java).use { context ->
            assertEquals("|#caption↕#|", context.getBean(VisualComponent::class.java).draw())
        }
    }

    @Test
    fun `a factory without an order goes last`() {
        this.start(Caption::class.java, DashBorderDecoratorFactory::class.java).use { context ->
            assertEquals("#-caption-↕#", context.getBean(VisualComponent::class.java).draw())
        }
    }

    @Test
    fun `the order of a bean definition counts`() {
        AnnotationConfigApplicationContext()
            .apply {
                register(
                    DecoratorFactoryBeanPostProcessor::class.java,
                    DecorationConfiguration::class.java,
                    Caption::class.java,
                )
                registerBean(
                    "dashBorderDecoratorFactory",
                    DashBorderDecoratorFactory::class.java,
                    BeanDefinitionCustomizer { definition ->
                        definition.setAttribute(AbstractBeanDefinition.ORDER_ATTRIBUTE, 0)
                    },
                )
                refresh()
            }
            .use { context -> assertEquals("-#caption↕#-", context.getBean(VisualComponent::class.java).draw()) }
    }

    @Test
    fun `factories of a type wrap the decorators of annotations`() {
        AnnotationConfigApplicationContext(
            DecoratedAnnotationBeanPostProcessor::class.java,
            DecoratorFactoryBeanPostProcessor::class.java,
            BorderStyle::class.java,
            DecorationConfiguration::class.java,
            TextArea::class.java,
        ).use { context -> assertEquals("##1text↕#1↕#", context.getBean(VisualComponent::class.java).draw()) }
    }

    @Test
    fun `a factory of another type is never made`() {
        this.start(Caption::class.java, RunnableDecoratorFactory::class.java).use { context ->
            assertEquals("#caption↕#", context.getBean(VisualComponent::class.java).draw())
        }
    }

    @Test
    fun `a bean of another type stays as it is`() {
        this.start(Label::class.java).use { context ->
            assertSame(Label::class.java, context.getBean(Label::class.java).javaClass)
        }
    }

    @Test
    fun `a factory that does not tell its type stops the startup`() {
        val failure = assertThrows<BeanCreationException> {
            AnnotationConfigApplicationContext()
                .apply {
                    register(DecoratorFactoryBeanPostProcessor::class.java, Caption::class.java)
                    registerBean(
                        "rawDecoratorFactory",
                        DecoratorFactory::class.java,
                        Supplier {
                            DecoratorFactory<Any> { target -> target }
                        },
                    )
                    refresh()
                }
        }

        assertEquals(
            "Decorator factory 'rawDecoratorFactory' does not tell the type it decorates: " +
                "declare it by a class or a @Bean method of type DecoratorFactory<T>",
            failure.mostSpecificCause.message,
        )
    }

    private fun start(vararg componentClasses: Class<*>) = AnnotationConfigApplicationContext(
        DecoratorFactoryBeanPostProcessor::class.java,
        DecorationConfiguration::class.java,
        *componentClasses,
    )
}
