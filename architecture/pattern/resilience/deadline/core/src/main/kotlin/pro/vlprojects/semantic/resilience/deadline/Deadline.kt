package pro.vlprojects.semantic.resilience.deadline

import pro.vlprojects.semantic.resilience.policy.Policy
import java.time.Duration
import java.util.concurrent.ThreadFactory

public class Deadline(
    private val limit: Duration,
    private val name: String = DEFAULT_NAME,
    listeners: List<DeadlineListener> = emptyList(),
    private val threadFactory: ThreadFactory = DEFAULT_THREAD_FACTORY,
) : Policy {

    init {
        require(limit.isPositive) { "The limit must be positive: $limit" }
    }

    private val listener = GuardedDeadlineListener(listeners)

    override fun <T> execute(action: () -> T): T = DeadlineCall(action, limit, name, listener, threadFactory).await()

    public companion object Factory {

        private const val DEFAULT_NAME = "unnamed"
        private val DEFAULT_THREAD_FACTORY = Thread
            .ofVirtual()
            .name("deadline-", 0)
            .factory()

        public fun of(limit: Duration): Deadline = Deadline(limit)

        public fun of(name: String, limit: Duration): Deadline = Deadline(limit, name)
    }
}
