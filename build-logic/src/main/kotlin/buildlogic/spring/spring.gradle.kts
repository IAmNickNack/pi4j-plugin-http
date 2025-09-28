package buildlogic.spring

import buildlogic.withVersionCatalog

plugins {
    id("buildlogic.java-core")
}


dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    withVersionCatalog {
        implementation(platform(libs.spring.dependencies))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
