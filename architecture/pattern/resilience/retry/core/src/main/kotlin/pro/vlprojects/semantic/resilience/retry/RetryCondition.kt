package pro.vlprojects.semantic.resilience.retry

import kotlin.reflect.KClass

public fun interface RetryCondition {

    public fun matches(failure: Throwable): Boolean

    public infix fun and(other: RetryCondition): RetryCondition = RetryCondition { failure ->
        matches(failure) && other.matches(failure)
    }

    public companion object Factory {

        private const val CAUSE_DEPTH_LIMIT = 32

        public fun ofAny(): RetryCondition = RetryCondition { true }

        public fun including(vararg types: KClass<out Throwable>): RetryCondition = RetryCondition { failure ->
            failure.isCausedByAny(types)
        }

        public fun excluding(vararg types: KClass<out Throwable>): RetryCondition = RetryCondition { failure ->
            !failure.isCausedByAny(types)
        }

        private fun Throwable.isCausedByAny(types: Array<out KClass<out Throwable>>) = generateSequence(
            this,
            Throwable::cause,
        )
            .take(CAUSE_DEPTH_LIMIT)
            .any { failure ->
                types.any { type -> type.isInstance(failure) }
            }
    }
}
