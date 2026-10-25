package pro.vlprojects.semantic.ddd.dsl

import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.specification.composite.conjunction
import pro.vlprojects.semantic.ddd.tactical.specification.composite.disjunction

public fun <T> allOf(vararg specifications: Specification<T>?): Specification<T> =
    specifications.filterNotNull().allOf()

public fun <T> anyOf(vararg specifications: Specification<T>?): Specification<T> =
    specifications.filterNotNull().anyOf()

public fun <T> Iterable<Specification<T>>.allOf(): Specification<T> = conjunction()

public fun <T> Iterable<Specification<T>>.anyOf(): Specification<T> = disjunction()
