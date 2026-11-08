package pro.vlprojects.semantic.resilience.extension

import pro.vlprojects.semantic.resilience.policy.Policy

public operator fun Policy.plus(inner: Policy): Policy = Policy.of(this, inner)

public operator fun <T> Policy.invoke(action: () -> T): T = this.execute(action)
