package pro.vlprojects.semantic.ddd.dsl

import pro.vlprojects.semantic.ddd.architecture.error.BusinessRuleException
import pro.vlprojects.semantic.ddd.tactical.BusinessRule
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun <T> rule(satisfied: T.() -> Boolean, message: T.() -> String): BusinessRule<T> =
    BusinessRule(satisfied, message)

public fun <T> Iterable<BusinessRule<T>>.check(target: T) {
    forEach { it.check(target) }
}

@OptIn(ExperimentalContracts::class)
public inline fun checkRule(condition: Boolean, message: () -> String) {
    contract {
        returns() implies condition
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
    }
    if (condition) return
    throw message().let(::BusinessRuleException)
}
