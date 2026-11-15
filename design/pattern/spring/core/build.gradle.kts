plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-pattern"))
    api(libs.spring.context)
    implementation(libs.spring.aop)
}
