package buildlogic.spring

import buildlogic.withVersionCatalog
import org.gradle.kotlin.dsl.kotlin

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    id("buildlogic.kotlin-core")
    id("buildlogic.spring.spring")
}

dependencies {
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    withVersionCatalog {
        testImplementation(libs.assertk)
    }
}