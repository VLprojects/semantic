package pro.vlprojects.semantic.ddd.dsl

import pro.vlprojects.semantic.ddd.tactical.Specification
import pro.vlprojects.semantic.ddd.tactical.specification.composite.conjoin
import pro.vlprojects.semantic.ddd.tactical.specification.composite.disjoin

public fun <T> allOf(vararg specifications: Specification<T>?): Specification<T> = specifications.asList().conjoin()

public fun <T> anyOf(vararg specifications: Specification<T>?): Specification<T> = specifications.asList().disjoin()

public fun <T> Iterable<Specification<T>>.allOf(): Specification<T> = conjoin()

public fun <T> Iterable<Specification<T>>.anyOf(): Specification<T> = disjoin()
