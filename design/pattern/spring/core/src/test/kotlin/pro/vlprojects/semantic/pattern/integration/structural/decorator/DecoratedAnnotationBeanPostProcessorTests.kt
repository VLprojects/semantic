package pro.vlprojects.semantic.pattern.integration.structural.decorator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.BeanNotOfRequiredTypeException
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import pro.vlprojects.semantic.pattern.integration.structural.decorator.scannable.Note
import pro.vlprojects.semantic.pattern.structural.decorator.DecoratedAnnotationBeanPostProcessor

class DecoratedAnnotationBeanPostProcessorTests {

    @Test
    fun `annotations decorate a bean in the order they are written`() {
        this.start(TextArea::class.java).use { context ->
            val area = context.getBean(VisualComponent::class.java)
            val border = assertInstanceOf(BorderDecorator::class.java, area)

            assertEquals("#1text↕#1", area.draw())
            assertEquals(ScrollDecorator::class, border.targetClass)
        }
    }

    @Test
    fun `a repeated annotation decorates once per repetition`() {
        this.start(Frame::class.java).use { context ->
            assertEquals("#1#2frame#2#1", context.getBean(VisualComponent::class.java).draw())
        }
    }

    @Test
    fun `a class with a decorating annotation is a component of the scan`() {
        AnnotationConfigApplicationContext()
            .apply {
                register(DecoratedAnnotationBeanPostProcessor::class.java, BorderStyle::class.java)
                scan(Note::class.java.packageName)
                refresh()
            }
            .use { context -> assertEquals("#1note#1", context.getBean(VisualComponent::class.java).draw()) }
    }

    @Test
    fun `a bean without decorating annotations stays as it is`() {
        this.start(Caption::class.java).use { context ->
            assertSame(Caption::class.java, context.getBean(VisualComponent::class.java).javaClass)
        }
    }

    @Test
    fun `a composed annotation does not decorate`() {
        this.start(Box::class.java).use { context ->
            assertSame(Box::class.java, context.getBean(VisualComponent::class.java).javaClass)
        }
    }

    @Test
    fun `an annotation is found behind an AOP proxy`() {
        this.start(TextArea::class.java, ProxyingBeanPostProcessor::class.java).use { context ->
            assertEquals("#1text°↕#1", context.getBean(VisualComponent::class.java).draw())
        }
    }

    @Test
    fun `a factory is made once per its class`() {
        this.start(TextArea::class.java, Frame::class.java).use { context ->
            assertEquals(1, context.getBean(BorderStyle::class.java).factories)
        }
    }

    @Test
    fun `a factory of another type than the bean stops the startup`() {
        val failure = assertThrows<BeanCreationException> { this.start(Label::class.java) }

        assertInstanceOf(BeanNotOfRequiredTypeException::class.java, failure.mostSpecificCause)
    }

    @Test
    fun `a factory of another annotation stops the startup`() {
        val failure = assertThrows<BeanCreationException> { this.start(Link::class.java) }

        assertEquals(
            "@${Underlined::class.java.name} of bean 'link' names ${BorderDecoratorFactory::class.java.name}, " +
                "which decorates by @${Bordered::class.java.name}",
            failure.mostSpecificCause.message,
        )
    }

    private fun start(vararg componentClasses: Class<*>) = AnnotationConfigApplicationContext(
        DecoratedAnnotationBeanPostProcessor::class.java,
        BorderStyle::class.java,
        *componentClasses,
    )
}
