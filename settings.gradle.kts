pluginManagement {
    // Convention plugins: each module says what kind it is, and its build follows from that.
    includeBuild("gradle/build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "semantic"

// A project is named after its artifact, a directory drops the prefix the path already gives:
// design/ddd/spring/data/jpa is semantic-ddd-spring-data-jpa.
fun module(name: String, directory: String) {
    include(name)
    project(":$name").projectDir = file(directory)
}

module("semantic-helper", "idiom/helper/core")
module("semantic-format", "idiom/format/core")

module("semantic-type-version", "type/version")

module("semantic-math", "math/core")

module("semantic-constraint", "correctness/constraint/core")
module("semantic-rule", "correctness/rule/core")
module("semantic-validation", "correctness/validation/core")
module("semantic-error", "correctness/error/core")

module("semantic-ddd", "design/ddd/core")
module("semantic-ddd-jackson", "design/ddd/jackson")
module("semantic-ddd-spring-data", "design/ddd/spring/data/core")
module("semantic-ddd-spring-data-jpa", "design/ddd/spring/data/jpa")
module("semantic-ddd-spring-data-mongodb", "design/ddd/spring/data/mongodb")
module("semantic-ddd-spring-boot", "design/ddd/spring-boot/autoconfigure")
module("semantic-ddd-spring-boot-starter-jpa", "design/ddd/spring-boot/starter/jpa")
module("semantic-ddd-spring-boot-starter-mongodb", "design/ddd/spring-boot/starter/mongodb")
module("semantic-dbc", "design/dbc/core")
module("semantic-algebra", "design/algebra/core")
module("semantic-pattern", "design/pattern/core")

module("semantic-layered", "architecture/style/layered/core")
module("semantic-onion", "architecture/style/onion/core")
module("semantic-clean", "architecture/style/clean/core")
module("semantic-explicit", "architecture/style/explicit/core")
module("semantic-boundary", "architecture/boundary/core")
module("semantic-cqrs", "architecture/pattern/persistence/cqrs/core")
module("semantic-event-sourcing", "architecture/pattern/persistence/event-sourcing/core")
module("semantic-unit-of-work", "architecture/pattern/persistence/unit-of-work/core")
module("semantic-delivery", "architecture/pattern/delivery/core")
module("semantic-delivery-jackson", "architecture/pattern/delivery/jackson")
module("semantic-delivery-observability-micrometer", "architecture/pattern/delivery/observability/micrometer")
module("semantic-delivery-test", "architecture/pattern/delivery/test")
module("semantic-delivery-spring", "architecture/pattern/delivery/spring/core")
module("semantic-delivery-spring-jdbc", "architecture/pattern/delivery/spring/jdbc")
module("semantic-delivery-spring-data-jpa", "architecture/pattern/delivery/spring/data/jpa")
module("semantic-delivery-spring-data-mongodb", "architecture/pattern/delivery/spring/data/mongodb")
module("semantic-delivery-spring-rabbitmq", "architecture/pattern/delivery/spring/rabbitmq")
module("semantic-delivery-spring-boot", "architecture/pattern/delivery/spring-boot/autoconfigure")
module("semantic-delivery-spring-boot-actuator", "architecture/pattern/delivery/spring-boot/actuator")
module("semantic-delivery-spring-boot-starter-jdbc", "architecture/pattern/delivery/spring-boot/starter/jdbc")
module("semantic-delivery-spring-boot-starter-jpa", "architecture/pattern/delivery/spring-boot/starter/jpa")
module("semantic-delivery-spring-boot-starter-mongodb", "architecture/pattern/delivery/spring-boot/starter/mongodb")
module("semantic-delivery-performance-test", "architecture/pattern/delivery/performance-test")
module("semantic-coordination", "architecture/pattern/coordination/core")
module("semantic-lifecycle", "architecture/pattern/lifecycle/core")
module("semantic-caching", "architecture/pattern/caching/core")
module("semantic-resilience", "architecture/pattern/resilience/core")
module("semantic-resilience-backoff", "architecture/pattern/resilience/backoff/core")
module("semantic-resilience-backoff-spring", "architecture/pattern/resilience/backoff/spring/core")
module("semantic-traffic", "architecture/pattern/traffic/core")
module("semantic-rollout", "architecture/pattern/rollout/core")
