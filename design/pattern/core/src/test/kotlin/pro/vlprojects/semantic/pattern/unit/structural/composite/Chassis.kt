package pro.vlprojects.semantic.pattern.unit.structural.composite

class Chassis(children: List<Equipment>) : Equipment() {

    override val children = children.toList()

    override val power
        get() = 20 + this.children.sumOf(Equipment::power)
}
