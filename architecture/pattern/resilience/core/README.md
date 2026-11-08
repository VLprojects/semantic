# semantic-resilience

`Policy` is a rule an action runs under: repeat it, reject it, limit it in time. The module holds that
type and the ways to combine policies. It depends on no pattern, so a service declares only the patterns
it uses.

The patterns themselves, `Retry`, `CircuitBreaker`, `Deadline`, `Bulkhead` and `Fallback`, come in modules
of their own and are not built yet. The examples below show how they are meant to be used.

## Run an action

```kotlin
val profile = retry.execute { http.get(id) }
```

The policy returns the result of the action, or lets its failure through once the rule has run out.

## Combine policies

A combination of policies is a `Policy` too. The first policy is the outermost, and each one surrounds
those after it. Keep a combination in a field and reuse it:

```kotlin
class ProfileClient(deadline: Deadline, breaker: CircuitBreaker, retry: Retry) {

    private val resilient = Policy.of(deadline, breaker, retry)

    fun load(id: ProfileId) = resilient.execute { http.get(id) }
}
```

This runs as:

```text
deadline
└─ breaker
   └─ retry
      └─ http.get(id)
```

The same combination can be written in other forms:

| Form | Import | Example |
| --- | --- | --- |
| factory | `policy.Policy` | `Policy.of(deadline, breaker, retry)` |
| infix | `dsl.then` | `deadline then breaker then retry` |
| operator | `extension.plus` | `deadline + breaker + retry` |

Calling a combination at once needs parentheses around the infix and the operator, and a long chain keeps
`then` or `+` at the end of a line:

```kotlin
Policy.of(breaker, retry).execute { http.get(id) }
(breaker then retry).execute { http.get(id) }
(breaker + retry).execute { http.get(id) }
```

With `extension.invoke` a policy runs like a function:

```kotlin
resilient { http.get(id) }

breaker {
    retry {
        http.get(id)
    }
}
```

## Order

The library does not enforce an order, since it changes the behaviour and each one has its use. This is
the recommended one, from the outermost:

```text
fallback
└─ deadline          one limit on the whole operation
   └─ circuit breaker
      └─ retry
         └─ bulkhead
            └─ the call
```

- A circuit breaker above retry counts one operation as one result. A failure that retry has healed does
  not count, and retry does not start while the breaker is open.
- Retry above a circuit breaker counts each attempt, so the breaker opens sooner.
- A limit on one attempt is the timeout of the HTTP client.

`Fallback` is not a `Policy`: its spare answer has a type of its own, while a `Policy` runs an action of any
type. It takes a ready policy and so always stays the outermost:

```kotlin
Fallback(Profile.EMPTY, resilient).execute { http.get(id) }
```

## Write a policy

`Policy` is a plain interface, because a generic method cannot be the single method of a `fun interface`:

```kotlin
class Timed(private val timer: Timer) : Policy {

    override fun <T> execute(action: () -> T): T = timer.record(action)
}
```

A policy of your own combines with the others in the same way.
