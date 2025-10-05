import kotlin.io.path.toPath

plugins {
    `kotlin-dsl`
    `version-catalog`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    api(files(libs.javaClass.protectionDomain.codeSource.location.toURI().toPath()))
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")
    implementation(libs.kotlin.spring)
    implementation(libs.spring.gradle)
    implementation("com.google.protobuf:protobuf-gradle-plugin:0.9.5")
}
