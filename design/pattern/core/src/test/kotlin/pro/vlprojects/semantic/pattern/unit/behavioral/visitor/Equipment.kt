package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

import pro.vlprojects.semantic.pattern.structural.composite.Composite

abstract class Equipment : Composite<Equipment> {

    abstract val price: Int

    abstract fun <R> accept(visitor: EquipmentVisitor<R>): R
}
