package pro.vlprojects.semantic.pattern.behavioral.resolver

public fun interface Candidates<out T : Any> : Iterable<T> {

    public fun isUnsatisfied(): Boolean = this.none()

    public fun isAmbiguous(): Boolean = this
        .asSequence()
        .drop(1)
        .any()
}
