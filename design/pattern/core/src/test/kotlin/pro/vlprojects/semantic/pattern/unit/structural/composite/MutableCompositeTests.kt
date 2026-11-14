package pro.vlprojects.semantic.pattern.unit.structural.composite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class MutableCompositeTests {

    private val disk = FloppyDisk()

    private val chassis = MutableChassis(listOf(disk))

    @Test
    fun `an added part is among the parts`() {
        val drives = Chassis(emptyList())

        chassis.add(drives)

        assertEquals(listOf(disk, drives), chassis.parts.toList())
    }

    @Test
    fun `a removed part is no longer among the parts`() {
        chassis.remove(disk)

        assertFalse(chassis.hasParts())
    }

    @Test
    fun `a part that is not there is not removed`() {
        chassis.remove(FloppyDisk())

        assertEquals(listOf(disk), chassis.parts.toList())
    }

    @Test
    fun `changes return the container for a chain of calls`() {
        val spare = FloppyDisk()

        assertSame(chassis, chassis.add(spare).remove(disk))
        assertEquals(listOf(spare), chassis.parts.toList())
    }

    @Test
    fun `an added part reaches the operation of the root`() {
        chassis.add(FloppyDisk())

        assertEquals(30, chassis.power)
    }

    @Test
    fun `an immutable copy keeps the parts of its moment`() {
        val copy = chassis.toImmutable()

        chassis.add(FloppyDisk())

        assertEquals(listOf(disk), copy.parts.toList())
        assertEquals(25, copy.power)
    }
}
