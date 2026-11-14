package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

import pro.vlprojects.semantic.pattern.behavioral.visitor.Visitor

class FloppyDisk : Equipment() {

    override val price = 5

    override fun <R> accept(visitor: Visitor<Equipment, R>) = when (visitor) {
        is EquipmentVisitor<R> -> visitor.visitFloppyDisk(this)
        else -> visitor.visit(this)
    }
}
