package pro.vlprojects.semantic.pattern.structural.adapter

/** The base type of an object adapter: equal to one of its class over an equal [adaptee], hidden from clients. */
public abstract class Adapter<out A : Any>(adaptee: A) {

    // Open, so that a class proxy of Spring reads the adaptee of its target, not an empty field.
    protected open val adaptee: A = adaptee

    final override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is Adapter<*> -> false
        this::class != other::class -> false
        else -> other.adaptee == this.adaptee
    }

    final override fun hashCode(): Int = this.adaptee.hashCode()

    override fun toString(): String = this.adaptee.toString()
}
