package pro.vlprojects.semantic.resilience.unit.backoff.spring

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.util.backoff.BackOffExecution
import pro.vlprojects.semantic.resilience.backoff.Backoff
import pro.vlprojects.semantic.resilience.backoff.spring.extension.toSpring
import java.time.Duration

class SpringBackOffTests {

    private val secondsByAttempt = Backoff { attempt ->
        attempt
            .toLong()
            .let(Duration::ofSeconds)
    }

    @Test
    fun `the pauses follow the backoff from the first attempt`() {
        val execution = secondsByAttempt.toSpring().start()

        assertEquals(1_000, execution.nextBackOff())
        assertEquals(2_000, execution.nextBackOff())
        assertEquals(3_000, execution.nextBackOff())
    }

    @Test
    fun `each start counts the attempts anew`() {
        val backOff = secondsByAttempt.toSpring()
        val first = backOff.start()
        first.nextBackOff()
        first.nextBackOff()

        assertEquals(1_000, backOff.start().nextBackOff())
        assertEquals(3_000, first.nextBackOff())
    }

    @Test
    fun `without a limit the pauses never stop`() {
        val execution = secondsByAttempt.toSpring().start()

        repeat(1_000) { index -> assertEquals((index + 1) * 1_000L, execution.nextBackOff()) }
    }

    @Test
    fun `with a limit the pauses stop once it is used up`() {
        val execution = secondsByAttempt.toSpring(maxAttempts = 2).start()

        assertEquals(1_000, execution.nextBackOff())
        assertEquals(2_000, execution.nextBackOff())
        assertEquals(BackOffExecution.STOP, execution.nextBackOff())
        assertEquals(BackOffExecution.STOP, execution.nextBackOff())
    }

    @Test
    fun `a limit of zero allows no pause`() {
        val execution = secondsByAttempt.toSpring(maxAttempts = 0).start()

        assertEquals(BackOffExecution.STOP, execution.nextBackOff())
    }

    @Test
    fun `a negative limit is refused`() {
        assertThrows<IllegalArgumentException> { secondsByAttempt.toSpring(maxAttempts = -1) }
    }
}
