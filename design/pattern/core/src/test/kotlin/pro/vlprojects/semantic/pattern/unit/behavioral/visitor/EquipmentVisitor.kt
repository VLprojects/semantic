package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

import pro.vlprojects.semantic.pattern.behavioral.visitor.Visitor

interface EquipmentVisitor<out R> : Visitor<Equipment, R> {

    fun visitFloppyDisk(disk: FloppyDisk) = visit(disk)

    fun visitChassis(chassis: Chassis) = visit(chassis)
}
