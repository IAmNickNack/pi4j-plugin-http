plugins {
    id("buildlogic.repositories")
    id("buildlogic.kotlin-core")
}

dependencies {
    implementation(project(":pi4j-plugin-http"))
    implementation(libs.pi4j.core)
}