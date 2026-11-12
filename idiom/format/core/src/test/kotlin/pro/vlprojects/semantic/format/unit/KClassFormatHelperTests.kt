package pro.vlprojects.semantic.format.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.format.toKebabCase

class KClassFormatHelperTests {

    @Test
    fun `a class is named in kebab case`() {
        assertEquals("shopping-cart-item", ShoppingCartItem::class.toKebabCase())
    }

    @Test
    fun `an anonymous class has no name`() {
        assertThrows<IllegalStateException> { object {}::class.toKebabCase() }
    }
}
