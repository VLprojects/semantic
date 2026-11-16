package pro.vlprojects.semantic.pattern.unit.structural.adapter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AdapterTests {

    private val view = TextView(width = 4, height = 2)

    @Test
    fun `an adapter answers the target through its adaptee`() {
        assertEquals(8, TextShape(view).area)
    }

    @Test
    fun `adapters of one class over equal adaptees are equal`() {
        val shape = TextShape(view)
        val same = TextShape(view.copy())

        assertEquals(shape, same)
        assertEquals(shape.hashCode(), same.hashCode())
        assertEquals(1, setOf(shape, same).size)
    }

    @Test
    fun `adapters over different adaptees are not equal`() {
        val other = TextView(width = 2, height = 4).let(::TextShape)

        assertNotEquals(TextShape(view), other)
    }

    @Test
    fun `an adapter and its subclass over one adaptee are not equal either way`() {
        assertNotEquals(TextShape(view), CaptionShape(view))
        assertNotEquals(CaptionShape(view), TextShape(view))
    }

    @Test
    fun `a container holds a child made again on another read of its children`() {
        val docs = Folder("docs")
        val root = Folder("root", listOf(docs)).let(::FolderComposite)
        val child = root.children.first()

        assertTrue(child in root.children)
    }

    @Test
    fun `an adapter is not equal to its adaptee`() {
        assertNotEquals(TextShape(view), view)
        assertNotEquals(view, TextShape(view))
    }

    @Test
    fun `an adapter reads as its adaptee`() {
        assertEquals(view.toString(), TextShape(view).toString())
    }
}
