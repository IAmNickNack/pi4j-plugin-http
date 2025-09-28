plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    `maven-publish`
}

dependencies {
    implementation(project(":pi4j-plugin-http-common"))
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.sse)
    implementation(libs.jackson.databind)
    implementation(libs.pi4j.core)
    implementation(libs.pi4j.plugin.mock)

    runtimeOnly(libs.logback.classic)

    testImplementation(platform(libs.spring.dependencies))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(project(":pi4j-plugin-http-server"))
    testImplementation(libs.pi4j.raspberrypi)
    testImplementation(libs.bundles.wiremock)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.iamnicknack.pi4j"
            artifactId = "pi4j-plugin-http"
            version = "0.0.1"
            from(components["java"])
        }
    }
}
