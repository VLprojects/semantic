package pro.vlprojects.semantic.resilience.retry

import java.time.Duration

public interface RetryListener {

    public fun onRetry(progress: RetryProgress, failure: Throwable, delay: Duration) {}

    public fun onSuccess(progress: RetryProgress) {}

    public fun onStop(progress: RetryProgress, failure: Throwable, reason: RetryStopReason) {}

    public companion object Factory {

        private val SILENT = object : RetryListener {}

        public fun ofSilent(): RetryListener = SILENT
    }
}
