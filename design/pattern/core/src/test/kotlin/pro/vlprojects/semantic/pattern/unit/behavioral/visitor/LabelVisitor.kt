package pro.vlprojects.semantic.pattern.unit.behavioral.visitor

import pro.vlprojects.semantic.pattern.behavioral.visitor.Visitor

class LabelVisitor : Visitor<Equipment, String> {

    override fun visit(element: Equipment) = "equipment"
}
