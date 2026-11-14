package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

import pro.vlprojects.semantic.pattern.behavioral.visitor.Visitable
import pro.vlprojects.semantic.pattern.structural.composite.Composite

abstract class Equipment :
    Composite<Equipment>,
    Visitable<Equipment> {

    abstract val price: Int
}
