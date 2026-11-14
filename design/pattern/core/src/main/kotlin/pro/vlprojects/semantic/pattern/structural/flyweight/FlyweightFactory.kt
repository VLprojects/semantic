package pro.vlprojects.semantic.pattern.structural.flyweight

/**
 * Hands out the shared flyweights of a domain: equal keys get the same instance while a client refers to it.
 * Safe for concurrent requests. A flyweight is immutable; a key compares by unchanging `equals` and `hashCode`.
 */
public interface FlyweightFactory<in K : Any, out F : Any> {

    public fun getFlyweight(key: K): F
}
