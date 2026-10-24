package pro.vlprojects.semantic.ddd.tactical

import pro.vlprojects.semantic.ddd.architecture.error.BusinessRuleException

public class BusinessRule<in T> internal constructor(
    private val satisfied: T.() -> Boolean,
    private val message: T.() -> String,
) {

    public fun check(target: T) {
        if (target.satisfied()) return
        throw target.message().let(::BusinessRuleException)
    }
}
