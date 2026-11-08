package pro.vlprojects.semantic.resilience.policy

public interface Policy {

    public fun <T> execute(action: () -> T): T

    public companion object Factory {

        public fun of(first: Policy, vararg others: Policy): Policy = others.fold(first, ::NestedPolicy)
    }
}
