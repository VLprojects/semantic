package pro.vlprojects.semantic.resilience.deadline

import java.time.Duration

internal class GuardedDeadlineListener(private val listeners: List<DeadlineListener>) : DeadlineListener {

    override fun onCompleted(elapsed: Duration) = notifyEach { it.onCompleted(elapsed) }

    override fun onFailed(elapsed: Duration, failure: Throwable) = notifyEach { it.onFailed(elapsed, failure) }

    override fun onExpired(limit: Duration) = notifyEach { it.onExpired(limit) }

    override fun onOverrun(elapsed: Duration) = notifyEach { it.onOverrun(elapsed) }

    private fun notifyEach(event: (DeadlineListener) -> Unit) = listeners.forEach { listener ->
        listener
            .runCatching(event)
            .onFailure(::reportFailure)
    }

    private fun reportFailure(failure: Throwable) = Thread
        .currentThread()
        .let { thread -> thread.uncaughtExceptionHandler.uncaughtException(thread, failure) }
}
