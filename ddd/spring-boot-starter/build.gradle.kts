plugins {
    id("semantic.starter")
}

dependencies {
    api(project(":semantic-ddd"))
    api(project(":semantic-ddd-jpa"))
    api(project(":semantic-ddd-spring-data"))
    api(project(":semantic-ddd-spring-boot"))
}
