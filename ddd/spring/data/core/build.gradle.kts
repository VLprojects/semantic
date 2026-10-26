plugins {
    id("semantic.kotlin-library")
}

dependencies {
    api(project(":semantic-ddd"))
    api(libs.spring.data.commons)
    implementation(project(":semantic-format"))
    // ProxyUtils of Spring Data needs AopUtils, which spring-data-commons leaves to the service.
    implementation(libs.spring.aop)
}
