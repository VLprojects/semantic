pluginManagement {
    // Convention plugins: each module says what kind it is, and its build follows from that.
    includeBuild("gradle/build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "semantic"

// A project is named after its artifact, a directory drops the prefix the path already gives:
// ddd/jpa is semantic-ddd-jpa.
fun module(name: String, directory: String) {
    include(name)
    project(":$name").projectDir = file(directory)
}

module("semantic-ddd", "ddd/core")
module("semantic-ddd-jpa", "ddd/jpa")
module("semantic-ddd-spring-data", "ddd/spring-data")
module("semantic-ddd-spring-boot", "ddd/spring-boot")
module("semantic-ddd-spring-boot-starter", "ddd/spring-boot-starter")

module("semantic-cqrs", "cqrs/core")
