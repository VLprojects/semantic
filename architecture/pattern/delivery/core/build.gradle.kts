plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-resilience"))
    implementation(project(":semantic-format"))
}
