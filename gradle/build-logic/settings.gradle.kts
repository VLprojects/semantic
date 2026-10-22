dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    // The same catalog as the main build, so a version is written once.
    versionCatalogs {
        create("libs") {
            from(files("../libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
