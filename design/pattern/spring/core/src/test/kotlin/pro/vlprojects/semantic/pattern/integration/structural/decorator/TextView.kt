package pro.vlprojects.semantic.pattern.integration.structural.decorator

class TextView(private val text: String) : VisualComponent {

    override fun draw() = this.text
}
