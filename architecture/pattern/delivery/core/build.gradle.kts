plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-resilience-backoff"))
    api(libs.slf4j.api)
    implementation(project(":semantic-format"))
}
