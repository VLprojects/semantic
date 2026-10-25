plugins {
    id("semantic.kotlin-jpa-library")
}

dependencies {
    api(project(":semantic-ddd-spring-data"))
    api(libs.jakarta.persistence.api)
    // Only to see through a Hibernate proxy when comparing entities; the service brings Hibernate itself.
    compileOnly(libs.hibernate.core)

    testImplementation(libs.hibernate.core)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.postgresql)
}

tasks.withType<Test>().configureEach {
    testLogging {
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
