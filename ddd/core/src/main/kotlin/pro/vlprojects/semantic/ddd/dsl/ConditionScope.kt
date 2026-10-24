package pro.vlprojects.semantic.ddd.dsl

import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.specification.AnyMatch
import pro.vlprojects.semantic.ddd.tactical.specification.AtLeast
import pro.vlprojects.semantic.ddd.tactical.specification.AtMost
import pro.vlprojects.semantic.ddd.tactical.specification.Condition
import pro.vlprojects.semantic.ddd.tactical.specification.EqualTo
import pro.vlprojects.semantic.ddd.tactical.specification.GreaterThan
import pro.vlprojects.semantic.ddd.tactical.specification.In
import pro.vlprojects.semantic.ddd.tactical.specification.LessThan
import pro.vlprojects.semantic.ddd.tactical.specification.NotEqualTo
import pro.vlprojects.semantic.ddd.tactical.specification.NotIn
import pro.vlprojects.semantic.ddd.tactical.specification.PropertyPath
import kotlin.reflect.KProperty1

public class ConditionScope<T> internal constructor() {

    public operator fun <V, W> KProperty1<T, V>.div(next: KProperty1<V & Any, W>): PropertyPath<T, W?> =
        PropertyPath(listOf(this, next))

    public operator fun <V, W> PropertyPath<T, V>.div(next: KProperty1<V & Any, W>): PropertyPath<T, W?> =
        PropertyPath(properties + next)

    public infix fun <V> KProperty1<T, V>.eq(value: V): Specification<T> = path eq value

    public infix fun <V> PropertyPath<T, V>.eq(value: V): Specification<T> = Condition(this, EqualTo(value))

    public infix fun <V> KProperty1<T, V>.notEq(value: V): Specification<T> = path notEq value

    public infix fun <V> PropertyPath<T, V>.notEq(value: V): Specification<T> = Condition(this, NotEqualTo(value))

    public infix fun <V> KProperty1<T, V>.isIn(values: Iterable<V>): Specification<T> = path isIn values

    public infix fun <V> PropertyPath<T, V>.isIn(values: Iterable<V>): Specification<T> =
        Condition(this, In(values.toSet()))

    public infix fun <V> KProperty1<T, V>.notIn(values: Iterable<V>): Specification<T> = path notIn values

    public infix fun <V> PropertyPath<T, V>.notIn(values: Iterable<V>): Specification<T> =
        Condition(this, NotIn(values.toSet()))

    public fun <V> KProperty1<T, V?>.isNull(): Specification<T> = path.isNull()

    public fun <V> PropertyPath<T, V?>.isNull(): Specification<T> = Condition(this, EqualTo(null))

    public fun <V> KProperty1<T, V?>.isNotNull(): Specification<T> = path.isNotNull()

    public fun <V> PropertyPath<T, V?>.isNotNull(): Specification<T> = Condition(this, NotEqualTo(null))

    public infix fun <V : Comparable<V>> KProperty1<T, V?>.lessThan(bound: V): Specification<T> = path lessThan bound

    public infix fun <V : Comparable<V>> PropertyPath<T, V?>.lessThan(bound: V): Specification<T> =
        Condition(this, LessThan(bound))

    public infix fun <V : Comparable<V>> KProperty1<T, V?>.atMost(bound: V): Specification<T> = path atMost bound

    public infix fun <V : Comparable<V>> PropertyPath<T, V?>.atMost(bound: V): Specification<T> =
        Condition(this, AtMost(bound))

    public infix fun <V : Comparable<V>> KProperty1<T, V?>.greaterThan(bound: V): Specification<T> =
        path greaterThan bound

    public infix fun <V : Comparable<V>> PropertyPath<T, V?>.greaterThan(bound: V): Specification<T> =
        Condition(this, GreaterThan(bound))

    public infix fun <V : Comparable<V>> KProperty1<T, V?>.atLeast(bound: V): Specification<T> = path atLeast bound

    public infix fun <V : Comparable<V>> PropertyPath<T, V?>.atLeast(bound: V): Specification<T> =
        Condition(this, AtLeast(bound))

    public fun <E> KProperty1<T, Iterable<E>?>.any(
        element: ConditionScope<E>.() -> Specification<E>,
    ): Specification<T> = path.any(element)

    public fun <E> PropertyPath<T, Iterable<E>?>.any(
        element: ConditionScope<E>.() -> Specification<E>,
    ): Specification<T> = Condition(this, AnyMatch(ConditionScope<E>().element()))

    private val <V> KProperty1<T, V>.path: PropertyPath<T, V>
        get() = PropertyPath(listOf(this))
}
