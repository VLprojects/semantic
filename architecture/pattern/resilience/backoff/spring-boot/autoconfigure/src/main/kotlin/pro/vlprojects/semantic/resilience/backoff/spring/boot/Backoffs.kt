package pro.vlprojects.semantic.resilience.backoff.spring.boot

import pro.vlprojects.semantic.resilience.backoff.Backoff

public class Backoffs(
    public val default: Backoff,
    private val named: Map<String, Backoff>,
) {

    public operator fun get(name: String): Backoff = named[name]
        ?: throw NoSuchElementException("No backoff is named '$name'. Named backoffs: ${named.keys}")

    public fun getOrDefault(name: String): Backoff = named[name] ?: default
}
