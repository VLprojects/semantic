package pro.vlprojects.semantic.pattern.unit.structural.composite

import pro.vlprojects.semantic.pattern.structural.composite.MutableComposite

class MutableChassis(children: List<Equipment>) :
    Equipment(),
    MutableComposite<Equipment> {

    override val children = children.toMutableList()

    override val power
        get() = 20 + this.children.sumOf(Equipment::power)

    override fun toImmutable() = Chassis(this.children)
}
