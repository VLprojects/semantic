plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-resilience-backoff-spring"))
    api(libs.spring.boot.autoconfigure)
    // Spring Boot reads the constructor parameters of Kotlin settings through kotlin-reflect.
    runtimeOnly(libs.kotlin.reflect)

    testImplementation(libs.spring.boot.test)
    testImplementation(libs.assertj.core)
}
