package pro.vlprojects.semantic.pattern.integration.structural.decorator

@Bordered(1)
@Scrolled
class TextArea : VisualComponent by TextView("text")
