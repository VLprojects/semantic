package pro.vlprojects.semantic.pattern.integration.structural.decorator.spring

@Bordered(1)
@Scrolled
class TextArea : VisualComponent by TextView("text")
