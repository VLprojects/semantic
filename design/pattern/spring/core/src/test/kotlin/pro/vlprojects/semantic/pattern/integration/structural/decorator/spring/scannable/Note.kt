package pro.vlprojects.semantic.pattern.integration.structural.decorator.spring.scannable

import pro.vlprojects.semantic.pattern.integration.structural.decorator.spring.Bordered
import pro.vlprojects.semantic.pattern.integration.structural.decorator.spring.TextView
import pro.vlprojects.semantic.pattern.integration.structural.decorator.spring.VisualComponent

@Bordered(1)
class Note : VisualComponent by TextView("note")
