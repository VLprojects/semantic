package pro.vlprojects.semantic.resilience.dsl

import pro.vlprojects.semantic.resilience.policy.Policy

public infix fun Policy.then(inner: Policy): Policy = Policy.of(this, inner)
