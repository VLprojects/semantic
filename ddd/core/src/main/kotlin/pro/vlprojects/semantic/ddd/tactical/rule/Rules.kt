package pro.vlprojects.semantic.ddd.tactical.rule

import pro.vlprojects.semantic.ddd.tactical.error.BusinessRuleException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * A rule over [T]. The condition is the statement that must hold, so the rule reads as an assertion; the message says
 * what is wrong when it does not:
 *
 * ```kotlin
 * rule<Pipeline>({ nodes.size <= 100 }) { "Pipeline $identifier has more than 100 nodes" }
 * ```
 */
public fun <T> rule(satisfied: T.() -> Boolean, message: T.() -> String): BusinessRule<T> =
    BusinessRule(satisfied, message)

/** Checks [target] against every rule in turn and throws on the first one it breaks. */
public fun <T> Iterable<BusinessRule<T>>.check(target: T) {
    forEach { it.check(target) }
}

/**
 * A one-off rule: throws [BusinessRuleException] unless [condition] holds. After the call the compiler knows that it
 * does, so a checked nullable is smart cast:
 *
 * ```kotlin
 * checkRule(order.paidAt != null) { "Order ${order.identifier} is not paid" }
 * ship(order.paidAt)
 * ```
 */
@OptIn(ExperimentalContracts::class)
public inline fun checkRule(condition: Boolean, message: () -> String) {
    contract {
        returns() implies condition
        callsInPlace(message, InvocationKind.AT_MOST_ONCE)
    }
    if (!condition) throw BusinessRuleException(message())
}
