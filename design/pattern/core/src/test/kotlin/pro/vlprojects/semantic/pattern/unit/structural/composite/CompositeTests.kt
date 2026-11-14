package pro.vlprojects.semantic.pattern.unit.structural.composite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompositeTests {

    private val disk = FloppyDisk()

    private val drives = Chassis(listOf(disk))

    private val spare = FloppyDisk()

    private val computer = Chassis(listOf(drives, spare))

    @Test
    fun `a leaf has no children`() {
        assertEquals(emptyList<Equipment>(), disk.children.toList())
        assertFalse(disk.hasChildren())
    }

    @Test
    fun `a container holds its children`() {
        assertEquals(listOf(drives, spare), computer.children.toList())
        assertTrue(computer.hasChildren())
    }

    @Test
    fun `an empty container has no children, as a leaf`() {
        assertFalse(Chassis(emptyList()).hasChildren())
    }

    @Test
    fun `the children of a child are of the hierarchy`() {
        assertSame(
            disk,
            computer.children
                .first()
                .children
                .first(),
        )
    }

    @Test
    fun `an operation of the root reaches every level`() {
        assertEquals(50, computer.power)
    }
}
