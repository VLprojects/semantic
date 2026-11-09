package pro.vlprojects.semantic.resilience.backoff.spring.boot.factory

import pro.vlprojects.semantic.resilience.backoff.Backoff

public interface BackoffFactory<C : Any> {

    public val type: String

    public val configClass: Class<C>

    public fun create(config: C): Backoff
}
