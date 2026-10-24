# semantic-cqrs

Commands and queries for Kotlin and Java, each carried out by exactly one
handler. The family does not depend on `ddd`: a service takes commands and
queries without aggregates, or aggregates without them.

The core holds the supertypes so far: messages, their handlers and buses. The
implementations of the buses come next.

## The core

Package `pro.vlprojects.semantic.cqrs` of `semantic-cqrs`:

| Type | What it is |
| --- | --- |
| `Command<R>` | an intent to change the state, answered with `R` |
| `Command.Handler<C, R>` | carries out commands of type `C` |
| `Command.Bus` | delivers a command to its one handler and returns what it answered |
| `Query<R>` | a question about the state, answered with `R` and changing nothing |
| `Query.Handler<Q, R>` | answers queries of type `Q` |
| `Query.Bus` | delivers a query to its one handler and returns the answer |

A message names its result type, `Command<OrderId>`, so whoever sends it gets
the result without a cast. The type argument of a handler,
`Command.Handler<PlaceOrder, OrderId>`, is the only place that says which
message it takes; a bus will find a handler by it. For the same reason a
handler is a class, not a lambda, and `Handler` is not a `fun interface`.

Handlers are synchronous and the core needs no coroutines. Should coroutines
be needed, a module of their own will bring a `suspend` handler and bus.

## Modules

One artifact so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-cqrs` | any JVM code: commands, queries, handlers and buses | nothing |

```
cqrs/
└── core/                  semantic-cqrs
```
