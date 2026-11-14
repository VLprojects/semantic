package pro.vlprojects.semantic.pattern.behavioral.visitor

/** An element that a [Visitor] visits: [accept] calls the visit method of its class or [Visitor.visit]. */
public interface Visitable<E> {

    public fun <R> accept(visitor: Visitor<E, R>): R
}
