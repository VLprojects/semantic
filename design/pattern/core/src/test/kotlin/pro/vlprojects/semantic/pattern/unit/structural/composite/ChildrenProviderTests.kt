package pro.vlprojects.semantic.pattern.unit.structural.composite

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.pattern.structural.composite.ChildrenProvider

class ChildrenProviderTests {

    private val folders = mapOf(
        "/" to listOf("/etc", "/usr"),
        "/usr" to listOf("/usr/bin"),
    )

    private val subfolders = ChildrenProvider<String> { path -> folders[path].orEmpty() }

    @Test
    fun `children of an element are read from outside its class`() {
        assertEquals(listOf("/etc", "/usr"), subfolders.getChildren("/").toList())
        assertTrue(subfolders.hasChildren("/"))
    }

    @Test
    fun `an element the provider knows no children of has none`() {
        assertEquals(emptyList<String>(), subfolders.getChildren("/etc").toList())
        assertFalse(subfolders.hasChildren("/etc"))
    }

    @Test
    fun `children of a composite are read through its property`() {
        val disk = FloppyDisk()
        val computer = Chassis(listOf(disk))
        val equipmentChildren = ChildrenProvider(Equipment::children)

        assertEquals(listOf(disk), equipmentChildren.getChildren(computer).toList())
        assertFalse(equipmentChildren.hasChildren(disk))
    }
}
