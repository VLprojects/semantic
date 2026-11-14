package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

class PricingVisitor : EquipmentVisitor<Int> {

    override fun visitChassis(chassis: Chassis) = chassis.price + chassis.children.sumOf { child -> child.accept(this) }

    override fun visit(element: Equipment) = element.price
}
