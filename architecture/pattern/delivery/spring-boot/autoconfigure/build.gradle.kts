plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-delivery-spring"))
    // An auto-configuration turns on only when the service has the module, so it never brings the module in.
    compileOnly(project(":semantic-delivery-jackson"))
    compileOnly(project(":semantic-delivery-observability-micrometer"))
    compileOnly(project(":semantic-delivery-spring-jdbc"))
    compileOnly(project(":semantic-delivery-spring-data-jpa"))
    compileOnly(project(":semantic-delivery-spring-data-mongodb"))
    compileOnly(project(":semantic-delivery-spring-rabbitmq"))
}
