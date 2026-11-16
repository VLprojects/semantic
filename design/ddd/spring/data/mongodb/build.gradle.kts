plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-ddd-spring-data"))
    api(libs.spring.data.mongodb)
    implementation(project(":semantic-format"))

    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.mongodb)
    // Spring Data MongoDB leaves the driver to the service.
    testImplementation(libs.mongodb.driver.sync)
    // Spring Data reads the parameters of a Kotlin constructor through kotlin-reflect.
    testRuntimeOnly(libs.kotlin.reflect)
}
