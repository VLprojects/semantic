package pro.vlprojects.semantic.resilience.backoff.spring.boot

import pro.vlprojects.semantic.resilience.backoff.Backoff

public class BackoffRegistry(
    public val default: Backoff,
    private val backoffs: Map<String, Backoff>,
) {

    public operator fun get(name: String): Backoff = backoffs[name]
        ?: throw NoSuchElementException("No backoff is named '$name'. Named backoffs: ${backoffs.keys}")

    public fun getOrDefault(name: String): Backoff = backoffs[name] ?: default
}
