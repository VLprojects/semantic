package pro.vlprojects.semantic.pattern.unit.structural.composite

import pro.vlprojects.semantic.pattern.structural.composite.Composite

abstract class Equipment : Composite<Equipment> {

    abstract val power: Int
}
