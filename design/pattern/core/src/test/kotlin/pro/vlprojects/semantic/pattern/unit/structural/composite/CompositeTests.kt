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
    fun `a leaf has no parts`() {
        assertEquals(emptyList<Equipment>(), disk.parts.toList())
        assertFalse(disk.hasParts())
    }

    @Test
    fun `a container holds its parts`() {
        assertEquals(listOf(drives, spare), computer.parts.toList())
        assertTrue(computer.hasParts())
    }

    @Test
    fun `an empty container has no parts, as a leaf`() {
        assertFalse(Chassis(emptyList()).hasParts())
    }

    @Test
    fun `the parts of a part are of the hierarchy`() {
        assertSame(
            disk,
            computer.parts
                .first()
                .parts
                .first(),
        )
    }

    @Test
    fun `an operation of the root reaches every level`() {
        assertEquals(50, computer.power)
    }
}
