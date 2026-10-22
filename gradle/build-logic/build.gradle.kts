plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    // kotlin("plugin.jpa") lives in the no-arg artifact.
    implementation(libs.kotlin.noarg)
    implementation(libs.kotlinter.gradle)
}
