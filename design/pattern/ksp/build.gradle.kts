plugins {
    id("semantic.kotlin-library")
}

dependencies {
    implementation(project(":semantic-pattern"))
    implementation(project(":semantic-ksp-helper"))
    compileOnly(libs.ksp.symbol.processing.api)

    testImplementation(libs.ksp.symbol.processing.api)
    testImplementation(libs.kctfork.ksp)
}
