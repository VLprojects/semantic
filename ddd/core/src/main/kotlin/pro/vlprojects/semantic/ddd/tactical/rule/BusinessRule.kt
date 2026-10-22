package pro.vlprojects.semantic.ddd.tactical.rule

import pro.vlprojects.semantic.ddd.architecture.error.BusinessRuleException

/**
 * A statement about the domain that must hold, written once and checked against many targets: an aggregate declares
 * its invariants once per type and shares them between its instances. Built with [rule].
 *
 * Unlike the standard `check`, a broken rule is a state of the domain, not a bug. It throws [BusinessRuleException],
 * which an adapter answers with 422 rather than 500.
 */
public class BusinessRule<in T> internal constructor(
    private val satisfied: T.() -> Boolean,
    private val message: T.() -> String,
) {
    /** Throws [BusinessRuleException] with the message of the rule if [target] breaks it. */
    public fun check(target: T) {
        if (!target.satisfied()) throw BusinessRuleException(target.message())
    }
}
