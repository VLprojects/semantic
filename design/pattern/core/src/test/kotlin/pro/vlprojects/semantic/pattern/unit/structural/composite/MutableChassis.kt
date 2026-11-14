package pro.vlprojects.semantic.pattern.unit.structural.composite

import pro.vlprojects.semantic.pattern.structural.composite.MutableComposite

class MutableChassis(parts: List<Equipment>) :
    Equipment(),
    MutableComposite<Equipment> {

    override val parts = parts.toMutableList()

    override val power
        get() = 20 + this.parts.sumOf(Equipment::power)

    override fun toImmutable() = Chassis(this.parts)
}
