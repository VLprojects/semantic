package pro.vlprojects.semantic.pattern.unit.structural.composite

class Chassis(parts: List<Equipment>) : Equipment() {

    override val parts = parts.toList()

    override val power
        get() = 20 + this.parts.sumOf(Equipment::power)
}
