package pro.vlprojects.semantic.pattern.integration.structural.decorator

@Bordered(1)
@Bordered(2)
class Frame : VisualComponent by TextView("frame")
