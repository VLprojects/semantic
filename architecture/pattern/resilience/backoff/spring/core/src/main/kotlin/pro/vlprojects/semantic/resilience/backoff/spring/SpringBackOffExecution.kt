package pro.vlprojects.semantic.resilience.backoff.spring

import org.springframework.util.backoff.BackOffExecution
import pro.vlprojects.semantic.resilience.backoff.Backoff

internal class SpringBackOffExecution(
    private val backoff: Backoff,
    private val maxAttempts: Int?,
) : BackOffExecution {

    private var attempt = 0

    override fun nextBackOff() = when (attempt) {
        maxAttempts -> BackOffExecution.STOP
        else -> calculateNextDelay()
    }

    private fun calculateNextDelay(): Long {
        attempt++
        return backoff.calculate(attempt).toMillis()
    }
}
