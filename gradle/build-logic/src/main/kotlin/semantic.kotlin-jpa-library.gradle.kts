// A Kotlin module with JPA mappings: Hibernate needs a no-arg constructor on every entity, embeddable and
// mapped superclass, and Kotlin does not write one.
plugins {
    id("semantic.kotlin-library")
    kotlin("plugin.jpa")
}
