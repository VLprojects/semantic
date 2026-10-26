plugins {
    id("semantic.starter")
}

dependencies {
    api(project(":semantic-ddd-spring-boot"))
    api(project(":semantic-ddd-spring-data-jpa"))
    api(libs.spring.boot.starter.data.jpa)
}
