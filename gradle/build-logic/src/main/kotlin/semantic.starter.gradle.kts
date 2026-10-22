// A starter: dependencies only, like the Spring Boot starters themselves.
plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
