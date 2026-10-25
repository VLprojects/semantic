plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-ddd"))
    api(libs.spring.data.commons)
    implementation(project(":semantic-format"))
}
