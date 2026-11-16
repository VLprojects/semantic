package pro.vlprojects.semantic.resilience.backoff.spring.boot.factory

import pro.vlprojects.semantic.resilience.backoff.Backoff

public interface BackoffFactory<C : Any> {

    public fun create(config: C): Backoff
}
