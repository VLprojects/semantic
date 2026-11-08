package pro.vlprojects.semantic.resilience

public fun Policy.around(inner: Policy): Policy = NestedPolicy(this, inner)
