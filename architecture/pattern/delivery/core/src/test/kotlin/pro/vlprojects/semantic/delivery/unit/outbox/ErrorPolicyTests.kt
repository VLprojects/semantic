package pro.vlprojects.semantic.delivery.unit.outbox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.delivery.outbox.RootCauseErrorPolicy
import pro.vlprojects.semantic.delivery.outbox.StackTraceErrorPolicy

class ErrorPolicyTests {

    private val rootCause = IllegalArgumentException("root")
    private val cause = IllegalStateException("wrapper", rootCause)

    @Test
    fun `the root cause policy describes the deepest cause`() {
        assertEquals("java.lang.IllegalArgumentException: root", RootCauseErrorPolicy().extract(cause))
    }

    @Test
    fun `the stack trace policy keeps the whole chain`() {
        val error = StackTraceErrorPolicy().extract(cause)

        assertTrue(error.contains("wrapper"))
        assertTrue(error.contains("root"))
    }
}
