# semantic-ddd-spring-boot

The auto-configuration of the ddd family. It holds no logic of its own: it
registers as beans the classes of the other modules, each when its module is
on the classpath and the service has not declared a bean of its own. A service
takes it through a starter, `semantic-ddd-spring-boot-starter-jpa` or
`-starter-mongodb`, not on its own.

Nothing needs configuring so far: the JPA bases map themselves with
annotations, and Spring Data publishes the events and tells a proxy from its
entity by itself. The packages below are laid out for what is to come.

## What it may hold

| Package | Auto-configuration | When | What it registers |
| --- | --- | --- | --- |
| `repository` | `SpecificationRepositoryAutoConfiguration` | `semantic-ddd-spring-data` and Querydsl are present | what a repository needs to find entities by a `Specification`: the metadata of the repository exposed to its fragments |
| `web` | `IdentifierConversionAutoConfiguration` | `semantic-ddd-spring-web` is present | the converter of a `String` to a `BoxedIdentifier`, so `@PathVariable id: CartId` and `@PathVariable cart: Cart` work |
| `mongodb` | `MongoAutoConfiguration` | `semantic-ddd-spring-data-mongodb` is present | the callbacks that mark an aggregate persisted on load and after save, the conversions of a key |
| `events` | `EventMetricsAutoConfiguration` | a `MeterRegistry` is present | a counter of the published events, tagged by the event and the aggregate |
| `verification` | `ModelVerificationAutoConfiguration` | `semantic.ddd.verify` is on | a check on start: a repository of an entity that is not a root, a key with no constructor JPA can call |

`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
lists the auto-configurations once the first of them is written.

The modules it configures are `compileOnly`: an auto-configuration turns on
when the service has the module, and the auto-configuration never brings one.
The starter of a store brings the modules of that store; any other module the
service adds itself.

## What it does not hold

- The converters, the Jackson module and the callbacks themselves: they live
  in the modules of their technology, `semantic-ddd-spring-web`,
  `semantic-ddd-jackson`, `semantic-ddd-spring-data-mongodb`, and work without
  Spring Boot.
- A bean the service cannot replace: every one is `@ConditionalOnMissingBean`.
- `@ComponentScan` or an `@Enable…` annotation, which would take over the
  configuration of the service.
- The mapping of errors to `ProblemDetail`, which belongs to the architecture
  family, and the outbox, which belongs to the events family.

Keys live under `semantic.ddd`. Should one area outgrow a package, it moves to a
module of its own, `semantic-ddd-spring-boot-<area>`, and the starters keep
their coordinates.
