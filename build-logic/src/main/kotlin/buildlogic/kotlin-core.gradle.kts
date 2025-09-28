package buildlogic

plugins {
    kotlin("jvm")
    id("buildlogic.java-core")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

dependencies {
    withVersionCatalog {
        implementation(platform(libs.kotlin.bom))
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}
