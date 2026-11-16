plugins {
    id("semantic.kotlin-library")
}

dependencies {
    compileOnly(libs.ksp.symbol.processing.api)

    testImplementation(libs.ksp.symbol.processing.api)
    testImplementation(libs.kctfork.ksp)
}
