package pro.vlprojects.semantic.resilience.deadline

import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class DeadlineCall<T>(
    private val action: () -> T,
    private val limit: Duration,
    private val name: String,
    private val listener: DeadlineListener,
    threadFactory: ThreadFactory,
) {

    private val startedAt = System.nanoTime()
    private val outcome = CompletableFuture<T>()
    private val worker = threadFactory.newThread(::perform)

    fun await(): T = try {
        worker.start()
        receive().also { listener.onCompleted(readElapsed()) }
    } catch (failure: ExecutionException) {
        val cause = failure.cause ?: failure
        listener.onFailed(readElapsed(), cause)
        throw cause
    } catch (interruption: InterruptedException) {
        outcome.completeExceptionally(interruption)
        worker.interrupt()
        throw interruption
    }

    private fun receive() = try {
        outcome.get(TimeUnit.NANOSECONDS.convert(limit), TimeUnit.NANOSECONDS)
    } catch (_: TimeoutException) {
        expire()
    }

    private fun expire(): T {
        DeadlineExceededException(name, limit)
            .takeIf(outcome::completeExceptionally)
            ?.let(::abandon)
        return outcome.get()
    }

    private fun abandon(exceeded: DeadlineExceededException): Nothing {
        worker.interrupt()
        listener.onExpired(limit)
        throw exceeded
    }

    private fun perform() = action
        .runCatching { invoke() }
        .fold(outcome::complete, outcome::completeExceptionally)
        .let(::reportOverrun)

    private fun reportOverrun(isDelivered: Boolean) = when {
        isDelivered -> Unit
        outcome.exceptionNow() is DeadlineExceededException -> listener.onOverrun(readElapsed())
        else -> Unit
    }

    private fun readElapsed() = System
        .nanoTime()
        .minus(startedAt)
        .let(Duration::ofNanos)
}
