plugins {
    id("buildlogic.repositories")
    id("buildlogic.spring.spring-boot")
}

dependencies {
    implementation(project(":pi4j-plugin-http-common"))
    implementation(project(":pi4j-plugin-http"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation(libs.spring.doc)
    implementation(libs.bundles.pi4j)
}

tasks.bootRun {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}


