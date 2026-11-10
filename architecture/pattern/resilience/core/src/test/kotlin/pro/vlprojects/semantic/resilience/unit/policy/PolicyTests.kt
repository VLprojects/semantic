package pro.vlprojects.semantic.resilience.unit.policy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import pro.vlprojects.semantic.resilience.dsl.then
import pro.vlprojects.semantic.resilience.extension.invoke
import pro.vlprojects.semantic.resilience.extension.plus
import pro.vlprojects.semantic.resilience.policy.Policy

class PolicyTests {

    private val trace = mutableListOf<String>()
    private val outer = tracing("outer")
    private val inner = tracing("inner")
    private val innermost = tracing("innermost")
    private val nested = listOf(
        "enter outer",
        "enter inner",
        "enter innermost",
        "action",
        "exit innermost",
        "exit inner",
        "exit outer",
    )

    @Test
    fun `the first policy of a combination is the outermost`() {
        Policy.of(outer, inner, innermost).execute { trace += "action" }

        assertEquals(nested, trace)
    }

    @Test
    fun `a single policy is its own combination`() {
        assertSame(outer, Policy.of(outer))
    }

    @Test
    fun `then surrounds the policy on its right`() {
        val combined = outer then inner then innermost

        combined.execute { trace += "action" }

        assertEquals(nested, trace)
    }

    @Test
    fun `plus surrounds the policy on its right`() {
        val combined = outer + inner + innermost

        combined.execute { trace += "action" }

        assertEquals(nested, trace)
    }

    @Test
    fun `a policy is invoked with its action`() {
        outer {
            inner {
                innermost {
                    trace += "action"
                }
            }
        }

        assertEquals(nested, trace)
    }

    @Test
    fun `the result of the action comes back`() {
        val result = Policy.of(outer, inner).execute { 42 }

        assertEquals(42, result)
    }

    @Test
    fun `the failure of the action comes back`() {
        val failure = IllegalStateException("down")

        val thrown = assertThrows<IllegalStateException> {
            Policy.of(outer, inner).execute { throw failure }
        }

        assertEquals(failure, thrown)
    }

    @Test
    fun `an outer policy that repeats runs the inner one each time`() {
        val repeating = repeating(3)

        Policy.of(repeating, inner).execute { trace += "action" }

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
