package pro.vlprojects.semantic.pattern.integration.structural.decorator.spring

@Bordered(1)
@Bordered(2)
class Frame : VisualComponent by TextView("frame")
