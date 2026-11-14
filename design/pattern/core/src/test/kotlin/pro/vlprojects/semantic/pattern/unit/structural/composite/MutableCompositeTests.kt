package pro.vlprojects.semantic.pattern.unit.structural.composite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class MutableCompositeTests {

    private val disk = FloppyDisk()

    private val chassis = MutableChassis(listOf(disk))

    @Test
    fun `an added child is among the children`() {
        val drives = Chassis(emptyList())

        chassis.add(drives)

        assertEquals(listOf(disk, drives), chassis.children.toList())
    }

    @Test
    fun `a removed child is no longer among the children`() {
        chassis.remove(disk)

        assertFalse(chassis.hasChildren())
    }

    @Test
    fun `a child that is not there is not removed`() {
        chassis.remove(FloppyDisk())

        assertEquals(listOf(disk), chassis.children.toList())
    }

    @Test
    fun `changes return the container for a chain of calls`() {
        val spare = FloppyDisk()

        assertSame(chassis, chassis.add(spare).remove(disk))
        assertEquals(listOf(spare), chassis.children.toList())
    }

    @Test
    fun `an added child reaches the operation of the root`() {
        chassis.add(FloppyDisk())

        assertEquals(30, chassis.power)
    }

    @Test
    fun `an immutable copy keeps the children of its moment`() {
        val copy = chassis.toImmutable()

        chassis.add(FloppyDisk())

        assertEquals(listOf(disk), copy.children.toList())
        assertEquals(25, copy.power)
    }
}
