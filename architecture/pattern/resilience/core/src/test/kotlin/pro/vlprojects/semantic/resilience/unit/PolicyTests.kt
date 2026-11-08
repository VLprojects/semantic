package pro.vlprojects.semantic.resilience.unit

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.Policy
import pro.vlprojects.semantic.resilience.around

class PolicyTests {

    private val trace = mutableListOf<String>()
    private val outer = tracing("outer")
    private val inner = tracing("inner")
    private val innermost = tracing("innermost")

    @Test
    fun `the outer policy surrounds the inner one`() {
        outer.around(inner).execute { trace += "action" }

        assertEquals(listOf("enter outer", "enter inner", "action", "exit inner", "exit outer"), trace)
    }

    @Test
    fun `a combination surrounds another policy like any policy`() {
        outer
            .around(inner)
            .around(innermost)
            .execute { trace += "action" }

        assertEquals(
            listOf(
                "enter outer",
                "enter inner",
                "enter innermost",
                "action",
                "exit innermost",
                "exit inner",
                "exit outer",
            ),
            trace,
        )
    }

    @Test
    fun `the result of the action comes back`() {
        val result = outer.around(inner).execute { 42 }

        assertEquals(42, result)
    }

    @Test
    fun `the failure of the action comes back`() {
        val failure = IllegalStateException("down")

        val thrown = assertThrows<IllegalStateException> {
            outer.around(inner).execute { throw failure }
        }

        assertEquals(failure, thrown)
    }

    @Test
    fun `an outer policy that repeats runs the inner one each time`() {
        val repeating = repeating(3)

        repeating.around(inner).execute { trace += "action" }

        assertEquals(3, trace.count { it == "enter inner" })
        assertEquals(3, trace.count { it == "action" })
    }

    private fun tracing(name: String) = object : Policy {

        override fun <T> execute(action: () -> T): T {
            trace += "enter $name"
            return action().also { trace += "exit $name" }
        }
    }

    private fun repeating(times: Int) = object : Policy {

        override fun <T> execute(action: () -> T) = (1..times)
            .map { action() }
            .last()
    }
}
