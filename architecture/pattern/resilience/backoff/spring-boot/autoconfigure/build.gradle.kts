plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-resilience-backoff"))
    api(libs.spring.boot.autoconfigure)
    runtimeOnly(libs.kotlin.reflect)
}
