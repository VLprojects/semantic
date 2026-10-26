# architecture

The layers of a service for Kotlin and Java: a class says which layer it
belongs to, and the rules of a project can check that the layers depend on
each other the right way. The family does not depend on `ddd` or `cqrs`.

The core holds the four layers so far. The rules that check them come next.

## The core

Package `pro.vlprojects.semantic.explicit.layer` of `semantic-explicit`:

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

## Integration

Package `pro.vlprojects.semantic.boundary` of `semantic-boundary`:

| Type | What it is |
| --- | --- |
| `@Integrated` | a class other code depends on by its shape: an event, a DTO, a message of an API |
| `Scope` | who depends on it: `COMPONENT_INTERNAL` for the modules of the same application, `SYSTEM_INTERNAL` for the other applications of the same system, `EXTERNAL` for other systems |
| `@IntroducedIn` | a property of an integrated class, with the `version` it appeared in |

The annotation holds what the class name cannot: a `namespace` and a `name`
that stay when the class is renamed, the `scope` of the integration, a
`version` that grows with each change of the contract and whether that version
`breaksBackwardCompatibility`: the consumers of the previous version can no
longer read it. `@IntroducedIn` marks the properties introduced after the first version.

```kotlin
@Integrated(
    namespace = "orders",
    name = "placed",
    scope = Scope.EXTERNAL,
    version = 3,
    breaksBackwardCompatibility = true,
)
class OrderPlaced(
    val orderId: String,
    @IntroducedIn(version = 3)
    val currency: String,
)
```

## Modules

Two artifacts with code so far, group `pro.vlprojects.semantic`:

| Artifact | For | Depends on |
| --- | --- | --- |
| `semantic-explicit` | any JVM code: the layers of a service | nothing |
| `semantic-boundary` | any JVM code: the contracts of a component with other systems | nothing |

```
architecture/
├── style/
│   └── explicit/core/     semantic-explicit
└── boundary/core/         semantic-boundary
```
