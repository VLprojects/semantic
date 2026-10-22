// A published Kotlin module: every module with code applies this one.
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation

plugins {
    kotlin("jvm")
    id("org.jmailen.kotlinter")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(25)
    explicitApi()
    compilerOptions {
        allWarningsAsErrors = true
        // A default method in an interface compiles to a JVM default method only. Without this it is also
        // copied to a $DefaultImpls class for Kotlin before 2.2, and that class would stay in the API for good.
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
    // A breaking change shows up as a diff of the checked-in dump.
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation()
}

dependencies {
    "testImplementation"(platform(libs.findLibrary("junit-bom").get()))
    "testImplementation"(libs.findLibrary("junit-jupiter").get())
    "testRuntimeOnly"(libs.findLibrary("junit-platform-launcher").get())
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
