package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

import pro.vlprojects.semantic.pattern.behavioral.visitor.Visitor

class Card : Equipment() {

    override val price = 30

    override fun <R> accept(visitor: Visitor<Equipment, R>) = visitor.visit(this)
}
