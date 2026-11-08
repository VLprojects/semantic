plugins {
    id("semantic.kotlin-jpa-library")
}

dependencies {
    api(project(":semantic-delivery-spring"))
    testImplementation(project(":semantic-delivery-test"))
}
