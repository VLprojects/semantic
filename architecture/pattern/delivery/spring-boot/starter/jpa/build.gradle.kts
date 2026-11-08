plugins {
    id("semantic.starter")
}

dependencies {
    api(project(":semantic-delivery-spring-boot"))
    api(project(":semantic-delivery-spring-data-jpa"))
    api(project(":semantic-delivery-jackson"))
    api(project(":semantic-delivery-observability-micrometer"))
}
