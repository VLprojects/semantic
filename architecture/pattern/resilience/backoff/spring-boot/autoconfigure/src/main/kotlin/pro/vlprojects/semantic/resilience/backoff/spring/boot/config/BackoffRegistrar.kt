package pro.vlprojects.semantic.resilience.backoff.spring.boot.config

import pro.vlprojects.semantic.resilience.backoff.spring.boot.BackoffRegistry

public fun interface BackoffRegistrar {

    public fun register(registry: BackoffRegistry)
}
