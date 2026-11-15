package pro.vlprojects.semantic.pattern.integration.structural.decorator.scannable

import pro.vlprojects.semantic.pattern.integration.structural.decorator.Bordered
import pro.vlprojects.semantic.pattern.integration.structural.decorator.TextView
import pro.vlprojects.semantic.pattern.integration.structural.decorator.VisualComponent

@Bordered(1)
class Note : VisualComponent by TextView("note")
