plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-ddd-jpa"))
    api(libs.spring.data.commons)
}
