package buildlogic.spring

import buildlogic.withVersionCatalog

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("buildlogic.kotlin-core")
    id("buildlogic.spring.spring")
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    withVersionCatalog {
        testImplementation(libs.assertk)
    }
}