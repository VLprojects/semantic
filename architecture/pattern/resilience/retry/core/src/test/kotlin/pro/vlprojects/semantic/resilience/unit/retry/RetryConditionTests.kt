package pro.vlprojects.semantic.resilience.unit.retry

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import pro.vlprojects.semantic.resilience.retry.RetryCondition
import java.io.FileNotFoundException
import java.io.IOException

class RetryConditionTests {

    @Test
    fun `any condition matches every failure`() {
        assertTrue(RetryCondition.ofAny().matches(IllegalStateException()))
    }

    @Test
    fun `an included type matches itself and its subtypes`() {
        val condition = RetryCondition.including(IOException::class)

        assertTrue(condition.matches(IOException()))
        assertTrue(condition.matches(FileNotFoundException()))
        assertFalse(condition.matches(IllegalStateException()))
    }

    @Test
    fun `an included type matches through the cause`() {
        val condition = RetryCondition.including(IOException::class)

        assertTrue(condition.matches(IllegalStateException(IOException())))
    }

    @Test
    fun `an excluded subtype wins over an included type`() {
        val condition =
            RetryCondition.including(IOException::class) and RetryCondition.excluding(FileNotFoundException::class)

        assertTrue(condition.matches(IOException()))
        assertFalse(condition.matches(FileNotFoundException()))
    }

    @Test
    fun `an excluded type does not match`() {
        val condition = RetryCondition.excluding(IOException::class)

        assertFalse(condition.matches(IllegalStateException(IOException())))
        assertTrue(condition.matches(IllegalStateException()))
    }
}
