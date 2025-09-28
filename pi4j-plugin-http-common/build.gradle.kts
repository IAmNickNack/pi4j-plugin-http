plugins {
    id("buildlogic.repositories")
    id("buildlogic.java-core")
    id("buildlogic.test.test-java")
    `maven-publish`
}

dependencies {
    implementation(libs.jackson.databind)
    implementation(libs.pi4j.core)
}


publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.iamnicknack.pi4j"
            artifactId = "pi4j-plugin-http-common"
            version = "0.0.1"
            from(components["java"])
        }
    }
}
