package pro.vlprojects.semantic.ddd.tactical

public interface Specification<in T> :
    DomainObject,
    (T) -> Boolean {

    public fun isSatisfiedBy(candidate: T): Boolean

    override fun invoke(candidate: T): Boolean = isSatisfiedBy(candidate)
}
