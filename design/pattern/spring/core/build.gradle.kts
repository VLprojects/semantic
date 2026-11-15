plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-pattern"))
    api(libs.spring.beans)
    implementation(libs.spring.aop)

    testImplementation(libs.spring.context)
}
