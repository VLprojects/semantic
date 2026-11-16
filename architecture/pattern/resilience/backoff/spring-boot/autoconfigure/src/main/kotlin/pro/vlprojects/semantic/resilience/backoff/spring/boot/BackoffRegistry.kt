package pro.vlprojects.semantic.resilience.backoff.spring.boot

import pro.vlprojects.semantic.resilience.backoff.Backoff
import java.util.concurrent.ConcurrentHashMap

public const val DEFAULT_BACKOFF_NAME: String = "default"

public class BackoffRegistry {

    private val backoffs = ConcurrentHashMap<String, Backoff>()

    public val names: Set<String>
        get() = backoffs.keys.toSet()

    public val default: Backoff
        get() = this[DEFAULT_BACKOFF_NAME]

    public operator fun get(name: String): Backoff = backoffs[name]
        ?: throw NoSuchElementException("No backoff is named '$name'. Named backoffs: ${backoffs.keys}")

    public fun getOrDefault(name: String): Backoff = backoffs[name] ?: default

    public fun register(name: String, backoff: Backoff): Unit = backoffs
        .putIfAbsent(name, backoff)
        .let { registered -> check(registered == null) { "The backoff '$name' is already registered" } }

    public fun replace(name: String, backoff: Backoff) {
        backoffs[name] = backoff
    }
}
