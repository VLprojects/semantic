package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

import pro.vlprojects.semantic.pattern.behavioral.visitor.Visitor

class Chassis(children: List<Equipment>) : Equipment() {

    override val children = children.toList()

    override val price = 20

    override fun <R> accept(visitor: Visitor<Equipment, R>) = when (visitor) {
        is EquipmentVisitor<R> -> visitor.visitChassis(this)
        else -> visitor.visit(this)
    }
}
