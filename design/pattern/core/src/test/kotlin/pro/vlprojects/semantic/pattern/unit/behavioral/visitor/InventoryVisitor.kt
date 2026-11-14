package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

class InventoryVisitor : EquipmentVisitor<List<String>> {

    override fun visitFloppyDisk(disk: FloppyDisk) = listOf("floppy disk")

    override fun visitChassis(chassis: Chassis) = chassis.children.flatMap { child -> child.accept(this) }

    override fun visit(element: Equipment) = emptyList<String>()
}
