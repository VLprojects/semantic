package pro.vlprojects.semantic.pattern.unit.structural.decorator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
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
    fun `a decorator tells the class of the delegate it wraps`() {
        assertEquals(TextView::class, ScrollDecorator(this.view).delegateClass)
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
    fun `a decorator in a chain tells the class of the next one`() {
        val window = this.view
            .let(::ScrollDecorator)
            .let { scroll -> BorderDecorator(scroll, 1) }

        assertEquals(ScrollDecorator::class, window.delegateClass)
    }
}
