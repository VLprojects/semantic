package pro.vlprojects.semantic.pattern.unit.structural.decorator

class TextView(override val text: String) : VisualComponent {

    override fun draw() = this.text
}
