package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

class Card : Equipment() {

    override val price = 30

    override fun <R> accept(visitor: EquipmentVisitor<R>) = visitor.visit(this)
}
