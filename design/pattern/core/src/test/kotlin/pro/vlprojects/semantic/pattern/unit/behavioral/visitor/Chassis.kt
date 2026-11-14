package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

class Chassis(children: List<Equipment>) : Equipment() {

    override val children = children.toList()

    override val price = 20

    override fun <R> accept(visitor: EquipmentVisitor<R>) = visitor.visitChassis(this)
}
