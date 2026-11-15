package pro.vlprojects.semantic.pattern.unit.structural.decorator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.structural.decorator.Decorate
import pro.vlprojects.semantic.pattern.structural.decorator.Decorator
import pro.vlprojects.semantic.pattern.structural.decorator.DecoratorFactory

class DecoratorTests {

    private val view = TextView("text")

    private val scroll = DecoratorFactory<VisualComponent>(::ScrollDecorator)

    @Test
    fun `a decorator adds its responsibility to the component`() {
        assertEquals("text↕", this.scroll.decorate(this.view).draw())
    }

    @Test
    fun `a decorator forwards what it does not change`() {
        assertEquals("text", this.scroll.decorate(this.view).text)
    }

    @Test
    fun `a decorator holds the delegate it wraps`() {
        assertSame(this.view, ScrollDecorator(this.view).delegate)
    }

    @Test
    fun `a decorator wraps another decorator`() {
        val window = this.scroll
            .decorate(this.view)
            .let { scroll -> BorderDecorator(scroll, 1) }

        assertEquals("|text↕|", window.draw())
        assertEquals("text", window.text)
    }

    @Test
    fun `a chain unwraps down to the component it started with`() {
        val window = this.view
            .let(::ScrollDecorator)
            .let { scroll -> BorderDecorator(scroll, 1) }
        val scroll = assertInstanceOf(Decorator::class.java, window.delegate)

        assertSame(this.view, scroll.delegate)
    }

    @Test
    fun `an annotation names the factories of its decorators`() {
        val decorate = Bordered::class.java.getAnnotation(Decorate::class.java)

        assertEquals(listOf(BorderDecoratorFactory::class), decorate.value.toList())
    }

    @Test
    fun `a factory configures a decorator by the annotation of the component`() {
        val caption = Caption("text")
        val annotation = Caption::class.java.getAnnotation(Bordered::class.java)

        assertEquals("||text||", BorderDecoratorFactory().decorate(caption, annotation).draw())
    }
}
