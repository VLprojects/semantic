plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(libs.spring.boot.autoconfigure)
    // An auto-configuration turns on only when the service has the module, so it never brings the module in.
    compileOnly(project(":semantic-ddd-spring-data"))
}
