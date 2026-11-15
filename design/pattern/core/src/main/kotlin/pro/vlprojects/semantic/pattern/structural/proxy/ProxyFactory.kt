package pro.vlprojects.semantic.pattern.structural.proxy

/**
 * Puts the target that [initialize] makes behind a proxy of its type, which calls it at once or on the first call.
 */
public fun interface ProxyFactory<T : Any> {

    public fun proxy(initialize: () -> T): T
}
