plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-ddd-spring-data"))
    api(libs.spring.boot.autoconfigure)
}
