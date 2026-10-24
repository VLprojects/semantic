# semantic-architecture

The layers of a service for Kotlin and Java: a class says which layer it
belongs to, and the rules of a project can check that the layers depend on
each other the right way. The family does not depend on `ddd` or `cqrs`.

The core holds the four layers so far. The rules that check them come next.

## The core

Package `pro.vlprojects.semantic.architecture.layer` of `semantic-architecture`:

| Type | What it is |
| --- | --- |
| `@PresentationLayer` | where requests come in: REST controllers, message listeners, CLI, schedulers |
| `@ApplicationLayer` | the use cases: it runs the domain and depends on it alone |
| `@DomainLayer` | the model and the rules of the business, depending on no other layer |
| `@InfrastructureLayer` | databases, brokers, clients of other services: what the inner layers ask for |

A layer is an annotation, not an interface: it says where a class lives and
asks nothing of it, so it stays out of the supertypes of the class. A Kotlin
file takes it as a whole with `@file:DomainLayer`.

```kotlin
@DomainLayer
class Order(...)

@ApplicationLayer
class PlaceOrderService(private val orders: Orders)

@InfrastructureLayer
class JpaOrders(private val repository: OrderRepository) : Orders

@PresentationLayer
class OrderController(private val placeOrder: PlaceOrderService)
```

## Modules

One artifact so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-architecture` | any JVM code: the layers of a service | nothing |

```
architecture/
└── core/                  semantic-architecture
```
