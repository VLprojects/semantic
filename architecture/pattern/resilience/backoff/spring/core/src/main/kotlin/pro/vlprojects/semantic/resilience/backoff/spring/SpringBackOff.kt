package pro.vlprojects.semantic.resilience.backoff.spring

import org.springframework.util.backoff.BackOff
import pro.vlprojects.semantic.resilience.backoff.Backoff

internal class SpringBackOff(
    private val backoff: Backoff,
    private val maxAttempts: Int?,
) : BackOff {

    init {
        maxAttempts?.also(::checkMaxAttemptsIsNotNegative)
    }

    override fun start() = SpringBackOffExecution(backoff, maxAttempts)

    private fun checkMaxAttemptsIsNotNegative(maxAttempts: Int) = require(maxAttempts >= 0) {
        "The max attempts must not be negative: $maxAttempts"
    }
}
