package pro.vlprojects.semantic.format.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.format.EllipsisMode
import pro.vlprojects.semantic.format.abbreviated
import pro.vlprojects.semantic.format.capitalizeEachPart
import pro.vlprojects.semantic.format.capitalizeFirstLetter
import pro.vlprojects.semantic.format.toCamelCase
import pro.vlprojects.semantic.format.toConstantCase
import pro.vlprojects.semantic.format.toKebabCase
import pro.vlprojects.semantic.format.toPascalCase
import pro.vlprojects.semantic.format.toSentenceCase
import pro.vlprojects.semantic.format.toSnakeCase
import pro.vlprojects.semantic.format.toTrainCase

class StringFormatHelperTests {

    @Test
    fun `kebab case joins the words with dashes`() {
        assertEquals("shopping-cart", "ShoppingCart".toKebabCase())
    }

    @Test
    fun `snake case joins the words with underscores`() {
        assertEquals("shopping_cart", "ShoppingCart".toSnakeCase())
    }

    @Test
    fun `camel case starts lower and capitalizes the next words`() {
        assertEquals("shoppingCart", "ShoppingCart".toCamelCase())
    }

    @Test
    fun `constant case writes the words in capitals with underscores`() {
        assertEquals("SHOPPING_CART", "ShoppingCart".toConstantCase())
    }

    @Test
    fun `pascal case capitalizes every word`() {
        assertEquals("ShoppingCart", "shopping-cart".toPascalCase())
    }

    @Test
    fun `train case capitalizes every word and joins them with dashes`() {
        assertEquals("Shopping-Cart", "shopping_cart".toTrainCase())
    }

    @Test
    fun `sentence case capitalizes the first word and joins them with spaces`() {
        assertEquals("Shopping cart", "SHOPPING_CART".toSentenceCase())
    }

    @Test
    fun `a value already in a case stays in it`() {
        assertEquals("shopping-cart", "shopping-cart".toKebabCase())
        assertEquals("shopping_cart", "shopping_cart".toSnakeCase())
        assertEquals("shoppingCart", "shoppingCart".toCamelCase())
        assertEquals("SHOPPING_CART", "SHOPPING_CART".toConstantCase())
        assertEquals("ShoppingCart", "ShoppingCart".toPascalCase())
        assertEquals("Shopping-Cart", "Shopping-Cart".toTrainCase())
        assertEquals("Shopping cart", "Shopping cart".toSentenceCase())
    }

    @Test
    fun `a case turns into any other`() {
        assertEquals("order-id", "order_id".toKebabCase())
        assertEquals("order_id", "orderId".toSnakeCase())
        assertEquals("orderId", "order-id".toCamelCase())
    }

    @Test
    fun `an acronym is one word`() {
        assertEquals("http-request", "HTTPRequest".toKebabCase())
        assertEquals("order-id", "OrderID".toKebabCase())
        assertEquals("xmlHttpRequest", "XMLHttpRequest".toCamelCase())
        assertEquals("XmlHttpRequest", "XMLHttpRequest".toPascalCase())
    }

    @Test
    fun `a digit ends a word`() {
        assertEquals("cart2-item", "Cart2Item".toKebabCase())
    }

    @Test
    fun `any run of what is not a letter or a digit separates words`() {
        assertEquals("order-line-item-id", " order.line\titem -- id! ".toKebabCase())
    }

    @Test
    fun `letters beyond latin keep their case rules`() {
        assertEquals("заказ-товара", "ЗаказТовара".toKebabCase())
        assertEquals("заказТовара", "заказ_товара".toCamelCase())
    }

    @Test
    fun `a combining mark stays in its word`() {
        assertEquals("cafe\u0301-item", "Cafe\u0301Item".toKebabCase())
    }

    @Test
    fun `a capital beyond the basic plane starts a word`() {
        assertEquals("order-𐐨𐐨", "order𐐀𐐨".toKebabCase())
    }

    @Test
    fun `a title case letter starts a word`() {
        assertEquals("order-ǆa", "orderǅa".toKebabCase())
    }

    @Test
    fun `a value with no letters or digits has no words`() {
        assertEquals("", "--".toKebabCase())
        assertEquals("", "".toCamelCase())
    }

    @Test
    fun `a long value keeps its start and end around the ellipsis`() {
        assertEquals("019…1d", ID.abbreviated())
    }

    @Test
    fun `the kept length can be set`() {
        assertEquals("0190…c41d", ID.abbreviated(keepLength = 8))
    }

    @Test
    fun `the beginning can be hidden`() {
        assertEquals("…ec41d", ID.abbreviated(mode = EllipsisMode.HideBeginning))
    }

    @Test
    fun `the ending can be hidden`() {
        assertEquals("0190a…", ID.abbreviated(mode = EllipsisMode.HideEnding))
    }

    @Test
    fun `a mode of its own hides what it chooses`() {
        assertEquals("0190a3f2-7…", ID.abbreviated(mode = { value, keep -> "${value.take(keep * 2)}…" }))
    }

    @Test
    fun `a value shorter than the minimum is kept whole`() {
        assertEquals("123456789", "123456789".abbreviated())
    }

    @Test
    fun `a value that would not get shorter is kept whole`() {
        assertEquals("1234567890", "1234567890".abbreviated(keepLength = 9))
    }

    @Test
    fun `a character beyond the basic plane counts as one`() {
        assertEquals("ab😀…yz", "ab😀cdefghijklmnopqrstuvwxyz".abbreviated())
        assertEquals("😀😀😀😀😀😀😀😀😀", "😀😀😀😀😀😀😀😀😀".abbreviated())
        assertEquals("🇷🇺🇷🇺🇷🇺…🇷🇺🇷🇺", "🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺🇷🇺".abbreviated())
    }

    @Test
    fun `a kept length that is not positive is refused`() {
        assertThrows<IllegalArgumentException> { ID.abbreviated(keepLength = 0) }
    }

    @Test
    fun `a minimum length not over the kept length is refused`() {
        assertThrows<IllegalArgumentException> { ID.abbreviated(minLength = 5, keepLength = 5) }
    }

    @Test
    fun `each part of a name starts with a capital`() {
        assertEquals("Jean-Luc", "jean-luc".capitalizeEachPart("-"))
        assertEquals("Anne Marie", "anne marie".capitalizeEachPart(" "))
    }

    @Test
    fun `the rest of a part keeps its case`() {
        assertEquals("McDonald O'Neil", "mcDonald o'Neil".capitalizeEachPart(" "))
    }

    @Test
    fun `a value without the separator is one part`() {
        assertEquals("Jean", "jean".capitalizeEachPart("-"))
    }

    @Test
    fun `empty parts stay empty`() {
        assertEquals("-Jean--Luc-", "-jean--luc-".capitalizeEachPart("-"))
    }

    @Test
    fun `an empty separator is refused`() {
        assertThrows<IllegalArgumentException> { "jean".capitalizeEachPart("") }
    }

    @Test
    fun `the first letter becomes a capital and the rest keeps its case`() {
        assertEquals("Order id", "order id".capitalizeFirstLetter())
        assertEquals("McDonald", "mcDonald".capitalizeFirstLetter())
        assertEquals("Élan", "élan".capitalizeFirstLetter())
    }

    @Test
    fun `a value without a lowercase start stays as it is`() {
        assertEquals("Order", "Order".capitalizeFirstLetter())
        assertEquals("42 orders", "42 orders".capitalizeFirstLetter())
        assertEquals("", "".capitalizeFirstLetter())
    }

    private companion object {

        const val ID = "0190a3f2-7b1e-7c3a-9f4e-5d2b8a6ec41d"
    }
}
