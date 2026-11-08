plugins {
    id("semantic.starter")
}

dependencies {
    api(project(":semantic-delivery-spring-boot"))
    api(project(":semantic-delivery-spring-jdbc"))
    api(project(":semantic-delivery-jackson"))
    api(project(":semantic-delivery-observability-micrometer"))
}
