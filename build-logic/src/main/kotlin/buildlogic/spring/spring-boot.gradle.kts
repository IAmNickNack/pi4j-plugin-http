package buildlogic.spring

plugins {
    // You can apply any plugins needed for Spring setup
    id("org.springframework.boot")// version "3.5.4" apply false
    id("buildlogic.spring.spring")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
