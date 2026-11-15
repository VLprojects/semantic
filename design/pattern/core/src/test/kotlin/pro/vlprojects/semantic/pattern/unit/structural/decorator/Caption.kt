package pro.vlprojects.semantic.pattern.unit.structural.decorator

@Bordered(2)
class Caption(text: String) : VisualComponent by TextView(text)
