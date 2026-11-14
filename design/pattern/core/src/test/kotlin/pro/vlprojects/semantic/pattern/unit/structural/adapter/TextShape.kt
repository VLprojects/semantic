package pro.vlprojects.semantic.pattern.unit.structural.adapter

import pro.vlprojects.semantic.pattern.structural.adapter.Adapter

open class TextShape(view: TextView) :
    Adapter<TextView>(view),
    Shape {

    override val area
        get() = this.adaptee.width * this.adaptee.height
}
