plugins {
    id("semantic.kotlin-jpa-library")
}

dependencies {
    api(project(":semantic-ddd"))
    api(libs.jakarta.persistence.api)
    // Only to see through a Hibernate proxy when comparing entities; the service brings Hibernate itself.
    compileOnly(libs.hibernate.core)
}
