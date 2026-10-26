plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-ddd"))
    api(libs.jackson.databind)

    testImplementation(libs.jackson.module.kotlin)
}
