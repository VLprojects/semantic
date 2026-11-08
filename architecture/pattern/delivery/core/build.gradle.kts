plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-resilience-backoff"))
    implementation(project(":semantic-format"))
}
