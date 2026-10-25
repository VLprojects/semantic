plugins {
    id("semantic.kotlin-jpa-library")
}

dependencies {
    api(project(":semantic-ddd-spring-data"))
    api(libs.jakarta.persistence.api)
    // Its detector is how ProxyUtils tells a Hibernate proxy from the class of the entity.
    api(libs.spring.data.jpa)

    testImplementation(libs.hibernate.core)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(libs.postgresql)
}
