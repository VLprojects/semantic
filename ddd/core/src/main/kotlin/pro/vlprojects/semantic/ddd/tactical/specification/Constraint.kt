package pro.vlprojects.semantic.ddd.tactical.specification

import pro.vlprojects.semantic.ddd.tactical.Specification

public sealed interface Constraint<in V> {

    public fun isSatisfiedBy(value: V): Boolean
}

public data class EqualTo<V>(
    val value: V,
) : Constraint<V> {

    override fun isSatisfiedBy(value: V): Boolean = value == this.value

    override fun toString(): String = "= $value"
}

public data class NotEqualTo<V>(
    val value: V,
) : Constraint<V> {

    override fun isSatisfiedBy(value: V): Boolean = value != this.value

    override fun toString(): String = "!= $value"
}

public data class In<V>(
    val values: Set<V>,
) : Constraint<V> {

    override fun isSatisfiedBy(value: V): Boolean = value in values

    override fun toString(): String = "in $values"
}

public data class NotIn<V>(
    val values: Set<V>,
) : Constraint<V> {

    override fun isSatisfiedBy(value: V): Boolean = value !in values

    override fun toString(): String = "not in $values"
}

public data class LessThan<V : Comparable<V>>(
    val bound: V,
) : Constraint<V?> {

    override fun isSatisfiedBy(value: V?): Boolean = value != null && value < bound

    override fun toString(): String = "< $bound"
}

public data class AtMost<V : Comparable<V>>(
    val bound: V,
) : Constraint<V?> {

    override fun isSatisfiedBy(value: V?): Boolean = value != null && value <= bound

    override fun toString(): String = "<= $bound"
}

public data class GreaterThan<V : Comparable<V>>(
    val bound: V,
) : Constraint<V?> {

    override fun isSatisfiedBy(value: V?): Boolean = value != null && value > bound

    override fun toString(): String = "> $bound"
}

public data class AtLeast<V : Comparable<V>>(
    val bound: V,
) : Constraint<V?> {

    override fun isSatisfiedBy(value: V?): Boolean = value != null && value >= bound

    override fun toString(): String = ">= $bound"
}

public data class AnyMatch<E>(
    val element: Specification<E>,
) : Constraint<Iterable<E>?> {

    override fun isSatisfiedBy(value: Iterable<E>?): Boolean = value?.any(element) == true

    override fun toString(): String = "any { $element }"
}
