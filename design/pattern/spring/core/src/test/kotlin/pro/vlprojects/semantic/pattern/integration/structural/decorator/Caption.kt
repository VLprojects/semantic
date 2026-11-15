package pro.vlprojects.semantic.pattern.integration.structural.decorator

import org.springframework.stereotype.Component

@Component
class Caption : VisualComponent by TextView("caption")
