package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

class FloppyDisk : Equipment() {

    override val price = 5

    override fun <R> accept(visitor: EquipmentVisitor<R>) = visitor.visitFloppyDisk(this)
}
