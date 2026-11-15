package pro.vlprojects.semantic.pattern.structural.decorator

/**
 * A wrapper of a [delegate] that implements the interface of its component itself,
 * forwards each request to the delegate and adds a responsibility before or after.
 */
public interface Decorator<out C : Any> {

    public val delegate: C
}
