package pro.vlprojects.semantic.format.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.format.kebabClassName

class AnyFormatHelperTests {

    @Test
    fun `a value is named after its class in kebab case`() {
        assertEquals("shopping-cart-item", ShoppingCartItem.kebabClassName)
    }

    @Test
    fun `a value of an anonymous class has no class name`() {
        assertThrows<IllegalStateException> { object {}.kebabClassName }
    }
}
