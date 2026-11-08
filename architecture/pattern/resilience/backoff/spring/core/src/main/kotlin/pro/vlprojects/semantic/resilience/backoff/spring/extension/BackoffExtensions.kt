package pro.vlprojects.semantic.resilience.backoff.spring.extension

import org.springframework.util.backoff.BackOff
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.SpringBackOff

public fun Backoff.toSpringBackOff(maxAttempts: Int? = null): BackOff = SpringBackOff(this, maxAttempts)
