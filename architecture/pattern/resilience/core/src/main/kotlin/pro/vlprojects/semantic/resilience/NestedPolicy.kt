package pro.vlprojects.semantic.resilience

internal class NestedPolicy(
    private val outer: Policy,
    private val inner: Policy,
) : Policy {

    override fun <T> execute(action: () -> T) = outer.execute { inner.execute(action) }
}
